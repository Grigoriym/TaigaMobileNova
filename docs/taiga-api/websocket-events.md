# Taiga WebSocket Events Documentation

## Overview

The Taiga backend sends real-time updates to the **taiga-events** service, which broadcasts changes to connected clients via WebSocket. This enables real-time collaboration where users see updates immediately when other team members make changes.

---

## Architecture

### Message Queue Backends

The backend supports two message queue implementations:

#### PostgreSQL NOTIFY/LISTEN (Default)
- **File**: `taiga/events/backends/postgresql.py`
- **Use case**: Simple deployments, development
- **Config**: `EVENTS_PUSH_BACKEND = "taiga.events.backends.postgresql.EventsPushBackend"`

#### RabbitMQ (Production)
- **File**: `taiga/events/backends/rabbitmq.py`
- **Use case**: Production, scalable deployments
- **Config**:
  ```python
  EVENTS_PUSH_BACKEND = "taiga.events.backends.rabbitmq.EventsPushBackend"
  EVENTS_PUSH_BACKEND_URL = "amqp://guest:guest@127.0.0.1/"
  ```

### Event Flow

```
┌─────────────────┐
│  Client Action  │
│  (API Request)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Django View/   │
│     Service     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   DB Transaction│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Django Signal   │
│ (post_save/     │
│  post_delete)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Event Queued    │
│ (on_commit)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Transaction     │
│   Commits       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Event Emitted   │
│ (PostgreSQL/    │
│  RabbitMQ)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Taiga-Events    │
│    Service      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   WebSocket     │
│   Broadcast     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Connected       │
│   Clients       │
└─────────────────┘
```

---

## Event Types

### 1. Model Change Events (Automatic)

These events are **automatically triggered** by Django signals whenever watched models are created, updated, or deleted.

#### Watched Models

| Model | App Label | Description |
|-------|-----------|-------------|
| User Story | `userstories.userstory` | User stories on backlog/kanban |
| Task | `tasks.task` | Tasks in sprints |
| Issue | `issues.issue` | Project issues |
| Epic | `epics.epic` | Epics (large features) |
| Milestone | `milestones.milestone` | Sprints/milestones |
| Wiki Page | `wiki.wiki_page` | Wiki documentation pages |
| User Story Status | `projects.userstorystatus` | Status configurations |
| Swimlane | `projects.swimlane` | Kanban swimlanes |
| Swimlane US Status | `projects.swimlaneuserstorystatus` | Swimlane-status mappings |

#### Event Structure

**Routing Key Format:**
```
changes.project.{project_id}.{app_label}.{model_name}
```

**Examples:**
- `changes.project.123.tasks.task`
- `changes.project.456.userstories.userstory`
- `changes.project.789.issues.issue`

**Message Payload:**

Single object change:
```json
{
  "session_id": "client-session-uuid",
  "data": {
    "type": "create",
    "matches": "userstories.userstory",
    "pk": 42
  }
}
```

Bulk operation (multiple objects):
```json
{
  "session_id": "client-session-uuid",
  "data": {
    "type": "change",
    "matches": "userstories.userstory",
    "pk": [1, 2, 3, 4, 5]
  }
}
```

**Event Types:**
- `"create"` - New object created
- `"change"` - Existing object updated
- `"delete"` - Object deleted

#### Trigger Points

| Action | Signal | Event Type |
|--------|--------|------------|
| Create user story | `post_save` (created=True) | `create` |
| Update task status | `post_save` (created=False) | `change` |
| Delete issue | `post_delete` | `delete` |
| Bulk reorder stories | Service method | `change` (multiple PKs) |
| Move task to column | `post_save` | `change` |

**Implementation:**
- File: `taiga/events/signal_handlers.py`
- Signal registration: `taiga/events/apps.py`

---

### 2. Bulk Operations Events

Bulk operations explicitly emit events for all affected objects.

#### Common Bulk Operations

| Operation | Service File | What Changes |
|-----------|--------------|--------------|
| Reorder user stories | `projects/services/bulk_update_order.py` | User story order field |
| Reorder tasks | `projects/services/bulk_update_order.py` | Task order field |
| Reorder epics | `projects/services/bulk_update_order.py` | Epic order field |
| Update epic user stories | `projects/epics/services.py` | Related user stories |
| Update swimlanes | `projects/services/bulk_update_order.py` | Swimlane order |
| Bulk status change | Various service files | Status field on multiple items |
| Bulk assignment | Various service files | Assigned user on multiple items |

#### Event Structure

```json
{
  "session_id": "client-session-uuid",
  "data": {
    "type": "change",
    "matches": "userstories.userstory",
    "pk": [12, 15, 18, 23, 27, 31]
  }
}
```

**Note:** Clients should refresh all objects in the `pk` array when receiving bulk events.

---

### 3. Web Notifications (User-Specific)

Events sent to specific users when they're involved in project activities.

#### Routing Key Format
```
web_notifications.{user_id}
```

**Example:** `web_notifications.42`

#### Triggers

| Trigger | When It Happens |
|---------|-----------------|
| User assigned | User is assigned to a user story, task, or issue |
| Multiple users assigned | Bulk assignment includes the user |
| Added as watcher | User is added to watchers list |
| Added as member | User is invited/added to project |
| Mentioned in object | User is @mentioned in description |
| Mentioned in comment | User is @mentioned in a comment |
| Comment added | Comment added to watched item |

#### Event Structure

```json
{
  "session_id": "originating-session-uuid",
  "data": {
    "type": "assigned",
    "user": {
      "id": 42,
      "username": "john.doe",
      "full_name": "John Doe",
      "photo": "https://..."
    },
    "project": {
      "id": 5,
      "name": "My Project",
      "slug": "my-project"
    },
    "object": {
      "id": 123,
      "ref": 456,
      "subject": "Implement authentication",
      "content_type": "userstories.userstory"
    },
    "created": "2026-01-05T10:30:00Z"
  }
}
```

**Implementation:**
- File: `taiga/projects/notifications/signals.py`
- Connected signals: Various model signals for assignments, mentions, watchers

---

### 4. Live Notifications (Browser Notifications)

Real-time browser notifications based on user notification policy settings.

#### Routing Key Format
```
live_notifications.{user_id}
```

**Example:** `live_notifications.42`

#### Triggers

Sent to users watching a project when:
- User story created/updated/deleted
- Task created/updated/deleted
- Issue created/updated/deleted
- Wiki page created/updated/deleted
- Milestone created/updated/deleted

**Notification Policy Levels:**
- **All**: Receive notifications for all changes
- **Involved**: Only when assigned or mentioned
- **None**: No notifications

#### Event Structure

```json
{
  "session_id": "originating-session-uuid",
  "data": {
    "title": "User story #42 created",
    "body": "My Project - Implement user authentication",
    "url": "https://taiga.io/project/my-project/us/42",
    "timeout": 10000,
    "id": 12345
  }
}
```

**Fields:**
- `title` - Notification headline
- `body` - Project name + item subject/title
- `url` - Deep link to the item
- `timeout` - Display duration in milliseconds
- `id` - History entry ID for tracking

**Implementation:**
- File: `taiga/projects/notifications/services.py`
- Called from: Service methods after model changes

---

### 5. Custom Notification Messages

Manual notifications sent via Django management command.

#### Routing Key
```
notifications
```

#### Usage

```bash
python manage.py emit_notification_message \
  --title "Maintenance Notice" \
  --desc "System will be down for maintenance at 2 AM UTC"
```

#### Event Structure

```json
{
  "session_id": null,
  "data": {
    "title": "Maintenance Notice",
    "desc": "System will be down for maintenance at 2 AM UTC"
  }
}
```

**Implementation:**
- File: `taiga/events/management/commands/emit_notification_message.py`

---

## Session ID Handling

### Purpose

Session IDs prevent **event echo** - when a client receives its own changes via WebSocket, causing duplicate UI updates.

### How It Works

1. **Client generates UUID** (e.g., `"a1b2c3d4-e5f6-7890-abcd-ef1234567890"`)
2. **Client sends session ID** in `x-session-id` HTTP header with all API requests
3. **Backend extracts session ID** via `SessionIDMiddleware`
4. **Backend includes session ID** in emitted events
5. **Taiga-Events forwards session ID** in WebSocket messages
6. **Client filters events** by comparing session IDs:
   - If `event.session_id == mySessionId` → **Ignore** (my own change)
   - If `event.session_id != mySessionId` → **Process** (someone else's change)

### Implementation

**Middleware:**
- File: `taiga/events/middleware.py`
- Class: `SessionIDMiddleware`
- Extracts `x-session-id` from request headers
- Stores in thread-local storage for access during request

**Configuration:**
- Added to `MIDDLEWARE` in `settings/common.py`

**Thread-Local Storage:**
```python
from taiga.events.middleware import get_current_session_id

def emit_event(routing_key, data):
    session_id = get_current_session_id()
    message = {
        "session_id": session_id,
        "data": data
    }
    backend.emit_event(routing_key, message)
```

---

## Client Implementation Guide

### For Mobile/Web Clients

#### 1. Generate Session ID

```kotlin
// Kotlin example

class ApiClient {
    private val sessionId = UUID.randomUUID().toString()

    // Add to all HTTP requests
    fun addHeaders(request: Request.Builder) {
        request.header("x-session-id", sessionId)
    }
}
```

#### 2. Connect to WebSocket

```kotlin
// Connect to taiga-events WebSocket endpoint
val wsUrl = "wss://taiga-events.example.com"
val socket = OkHttpWebSocketClient(wsUrl)

socket.connect()
```

#### 3. Subscribe to Routing Keys

```kotlin
// Subscribe to project changes
socket.subscribe("changes.project.$projectId.*")

// Subscribe to user notifications
socket.subscribe("web_notifications.$userId")
socket.subscribe("live_notifications.$userId")
```

#### 4. Handle Incoming Events

```kotlin
socket.onMessage { message ->
    val event = parseWebSocketEvent(message)

    // Filter out our own changes
    if (event.sessionId == sessionId) {
        return@onMessage // Ignore echo
    }

    when (event.routingKey) {
        matches("changes.project.\\d+.*") -> {
            handleModelChange(event)
        }
        matches("web_notifications.*") -> {
            handleWebNotification(event)
        }
        matches("live_notifications.*") -> {
            handleLiveNotification(event)
        }
    }
}
```

#### 5. Process Model Changes

```kotlin
fun handleModelChange(event: WebSocketEvent) {
    val data = event.data

    when (data.type) {
        "create" -> {
            // Fetch new object from API
            val newObject = api.getObject(data.matches, data.pk)
            cache.add(newObject)
            ui.addToList(newObject)
        }

        "change" -> {
            // Refresh object(s) from API
            if (data.pk is List) {
                // Bulk update
                data.pk.forEach { id ->
                    val updated = api.getObject(data.matches, id)
                    cache.update(updated)
                    ui.updateInList(updated)
                }
            } else {
                // Single update
                val updated = api.getObject(data.matches, data.pk)
                cache.update(updated)
                ui.updateInList(updated)
            }
        }

        "delete" -> {
            // Remove from cache and UI
            cache.remove(data.matches, data.pk)
            ui.removeFromList(data.pk)
        }
    }
}
```

#### 6. Handle Notifications

```kotlin
fun handleWebNotification(event: WebSocketEvent) {
    val notification = event.data

    // Add to notification list
    notificationRepository.addNotification(notification)

    // Update badge count
    ui.updateNotificationBadge()
}

fun handleLiveNotification(event: WebSocketEvent) {
    val data = event.data

    // Show OS notification
    notificationManager.show(
        title = data.title,
        body = data.body,
        url = data.url,
        timeout = data.timeout
    )
}
```

---

## WebSocket Message Format

### Complete Message Structure

```json
{
  "routing_key": "changes.project.5.userstories.userstory",
  "session_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "type": "change",
    "matches": "userstories.userstory",
    "pk": 123
  }
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `routing_key` | String | Message routing pattern (determines event type) |
| `session_id` | String or null | UUID of the originating client session |
| `data` | Object | Event-specific payload (structure varies by event type) |

---

## Routing Key Patterns

### Pattern Matching

Use pattern matching to handle different event types:

| Pattern | Regex | Description |
|---------|-------|-------------|
| Model changes | `^changes\.project\.(\d+)\.(.+)$` | Project model CRUD events |
| Web notifications | `^web_notifications\.(\d+)$` | User-specific notifications |
| Live notifications | `^live_notifications\.(\d+)$` | Browser notifications |
| System notifications | `^notifications$` | System-wide messages |

### Subscription Wildcards

When subscribing to routing keys, use wildcards:

- `*` - Match single segment
- `#` - Match zero or more segments

**Examples:**
```
changes.project.5.*              # All changes in project 5
changes.project.5.userstories.*  # Only user story changes in project 5
changes.project.#                # All changes in all projects
web_notifications.42             # Notifications for user 42
```

---

## Transaction Handling

### Event Queue on Commit

Events are queued and only sent **after the database transaction commits** successfully.

**Implementation:**
```python
from django.db import connection

def emit_event(routing_key, data, on_commit=True):
    if on_commit:
        connection.on_commit(lambda: _do_emit_event(routing_key, data))
    else:
        _do_emit_event(routing_key, data)
```

**Benefits:**
- ✅ **Consistency**: Events only sent if data is persisted
- ✅ **No phantom updates**: Clients never see changes that got rolled back
- ✅ **Order preservation**: Events arrive in commit order

**Edge Cases:**
- Live notifications use `on_commit=False` for immediate delivery
- Management commands bypass transaction handling

---

## Key Source Files

### Core Event System

| File | Purpose |
|------|---------|
| `taiga/events/events.py` | Event emission functions |
| `taiga/events/signal_handlers.py` | Django signal receivers |
| `taiga/events/apps.py` | App configuration and signal registration |
| `taiga/events/middleware.py` | Session ID middleware |

### Backend Implementations

| File | Purpose |
|------|---------|
| `taiga/events/backends/base.py` | Abstract backend interface |
| `taiga/events/backends/postgresql.py` | PostgreSQL NOTIFY backend |
| `taiga/events/backends/rabbitmq.py` | RabbitMQ AMQP backend |

### Notification System

| File | Purpose |
|------|---------|
| `taiga/projects/notifications/signals.py` | Web notification signal handlers |
| `taiga/projects/notifications/services.py` | Live notification service |

### Service Layer

| File | Purpose |
|------|---------|
| `taiga/projects/services/bulk_update_order.py` | Bulk operation events |
| `taiga/projects/userstories/services.py` | User story events |
| `taiga/projects/tasks/services.py` | Task events |
| `taiga/projects/issues/services.py` | Issue events |
| `taiga/projects/epics/services.py` | Epic events |
| `taiga/projects/milestones/services.py` | Milestone events |

### Management Commands

| File | Purpose |
|------|---------|
| `taiga/events/management/commands/emit_notification_message.py` | Manual notification emission |

---

## Configuration Reference

### Settings

```python
# Backend selection (choose one)
EVENTS_PUSH_BACKEND = "taiga.events.backends.postgresql.EventsPushBackend"
# OR
EVENTS_PUSH_BACKEND = "taiga.events.backends.rabbitmq.EventsPushBackend"

# RabbitMQ configuration (if using RabbitMQ backend)
EVENTS_PUSH_BACKEND_URL = "amqp://user:password@rabbitmq-host:5672/"

# Session ID middleware (required)
MIDDLEWARE = [
    # ... other middleware
    'taiga.events.middleware.SessionIDMiddleware',
]
```

### Environment Variables (Docker)

```bash
# RabbitMQ backend URL
EVENTS_PUSH_BACKEND_URL=amqp://taiga:taiga@rabbitmq:5672/taiga

# Taiga-events WebSocket URL (for frontend)
TAIGA_EVENTS_URL=wss://events.taiga.example.com
```

---

## Testing WebSocket Events

### Using Browser Console

```javascript
// Connect to taiga-events WebSocket
const ws = new WebSocket('ws://localhost:8888/events');

ws.onopen = () => {
    console.log('Connected to taiga-events');

    // Subscribe to project changes
    ws.send(JSON.stringify({
        cmd: 'subscribe',
        routing_key: 'changes.project.5.*'
    }));
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('Received event:', data);
};
```

### Using curl to Trigger Events

```bash
# Create a user story (triggers "create" event)
curl -X POST http://localhost:9000/api/v1/userstories \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "x-session-id: test-session-123" \
  -H "Content-Type: application/json" \
  -d '{
    "project": 5,
    "subject": "Test user story",
    "status": 1
  }'

# Update a user story (triggers "change" event)
curl -X PATCH http://localhost:9000/api/v1/userstories/123 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "x-session-id: test-session-123" \
  -H "Content-Type: application/json" \
  -d '{"subject": "Updated subject"}'

# Delete a user story (triggers "delete" event)
curl -X DELETE http://localhost:9000/api/v1/userstories/123 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "x-session-id: test-session-123"
```

### Monitoring Events (PostgreSQL Backend)

```sql
-- Listen to PostgreSQL NOTIFY events
LISTEN events;

-- In another terminal, trigger an action via API
-- You'll see: Asynchronous notification "events" with payload "..." received
```

### Monitoring Events (RabbitMQ Backend)

```bash
# Monitor RabbitMQ queues
rabbitmqctl list_queues

# View exchange bindings
rabbitmqctl list_bindings

# Monitor messages
rabbitmqadmin get queue=events count=10
```

---

## Troubleshooting

### Events Not Received

**Checklist:**
1. ✅ Is taiga-events service running?
2. ✅ Is WebSocket connection established?
3. ✅ Are you subscribed to the correct routing key?
4. ✅ Is the session ID filtering out your own events?
5. ✅ Is the backend configured correctly (PostgreSQL/RabbitMQ)?
6. ✅ Are Django signals connected? (Check `taiga/events/apps.py`)

### Duplicate Events

**Possible causes:**
- Session ID not being sent in HTTP headers
- Multiple WebSocket connections with same subscriptions
- Event handler called multiple times due to signal misconfiguration

### Event Lag

**Possible causes:**
- Network latency between services
- RabbitMQ queue backlog
- Database transaction delays
- taiga-events service overloaded

**Solutions:**
- Use RabbitMQ backend for better scalability
- Monitor queue depth and processing time
- Scale taiga-events service horizontally

---

## Best Practices

### For Backend Developers

1. **Always use `on_commit=True`** for data consistency
2. **Include session_id** in all events
3. **Use bulk events** for operations affecting multiple objects
4. **Test signal handlers** to ensure events are emitted
5. **Monitor event queue** depth in production

### For Frontend/Mobile Developers

1. **Generate unique session ID** per app instance
2. **Send session ID** with all API requests
3. **Filter own events** to prevent duplicate updates
4. **Subscribe to specific routing keys** (not wildcards in production)
5. **Handle reconnection** gracefully
6. **Implement exponential backoff** for reconnection attempts
7. **Cache event data** for offline support
8. **Batch UI updates** to prevent flickering

---

## Additional Resources

### Related Documentation

- Django Signals: https://docs.djangoproject.com/en/stable/topics/signals/
- RabbitMQ Routing: https://www.rabbitmq.com/tutorials/tutorial-five-python.html
- WebSocket Protocol: https://tools.ietf.org/html/rfc6455

### Taiga-Events Service

The taiga-events service is a separate component that acts as a WebSocket gateway:

- **Repository**: https://github.com/taigaio/taiga-events
- **Technology**: Node.js, RabbitMQ/PostgreSQL client
- **Purpose**: Bridge between backend events and WebSocket clients
- **Deployment**: Typically runs alongside taiga-back

---

## Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2026-01-05 | 1.0 | Initial documentation |

---

## License

This documentation is part of the Taiga project and follows the same license:
- Mozilla Public License, v. 2.0
- Copyright (c) 2021-present Kaleidos INC
