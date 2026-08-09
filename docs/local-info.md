This info for my local setup

http://localhost:9000/

The `taiga-mcp` MCP server (`taiga_login` / `taiga_request` / `taiga_auth_status`) is already
configured to reach this instance — use it instead of curl for ad hoc API checks.

setup:
    https://community.taiga.io/t/taiga-30min-setup/170/1

docker compose -f docker-compose.yml -f docker-compose-inits.yml run --rm taiga-manage createsuperuser
docker compose -f docker-compose.yml -f docker-compose-inits.yml run --rm taiga-manage changepassword

to create a new user go to /admin/ panel

users:
    admin - admin
    user1 - user1 - user1@mail.com
    user2 - user2 - user2@mail.com
    user3 - user3 - user3@mail.com