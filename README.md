# bruitage

A Discord music bot written in Kotlin. Plays music in voice channels, manages a queue, and provides
AI-powered commentary on tracks.

## Features

- Plays music from any source supported by Lavalink
- Queue management (view, skip, stop)
- Sponsorblock integration to automatically skip sponsored segments
- Silly AI-generated commentary on currently playing tracks

## Commands

| Command         | Description                                      |
|-----------------|--------------------------------------------------|
| `/play <query>` | Add a track to the queue (YouTube search or URL) |
| `/skip`         | Skip to the next track                           |
| `/queue`        | View the current queue                           |
| `/stop`         | Stop playback and clear the queue                |

## Requirements

- Java 21+
- A running [Lavalink](https://github.com/lavalink-devs/Lavalink) server
- A Discord bot token and application ID

## Configuration

Set the following environment variables before running:

| Variable            | Required | Description                                             |
|---------------------|----------|---------------------------------------------------------|
| `BOT_TOKEN`         | Yes      | Discord bot token                                       |
| `BOT_CLIENT_ID`     | Yes      | Discord application/client ID                           |
| `LAVALINK_URI`      | Yes      | URI of the Lavalink server (e.g. `ws://localhost:2333`) |
| `LAVALINK_PASSWORD` | Yes      | Lavalink server password                                |
| `MISTRAL_API_KEY`   | No       | Mistral AI API key for track commentary                 |

A sample Lavalink configuration is provided in
[`lavalink/application.yaml`](lavalink/application.yaml).

## Running

### With Gradle

```sh
./gradlew app:run
```

### With Docker

A pre-built Docker image is published to the GitHub Container Registry on every push to `main`:

```sh
docker pull ghcr.io/outadoc/bruitage:latest
```

Using docker-compose:

```yaml
services:
  bruitage:
    image: ghcr.io/outadoc/bruitage:latest
    container_name: bruitage
    depends_on:
      - lavalink
    environment:
      - LAVALINK_URI=ws://lavalink:2333
      - BOT_CLIENT_ID=${BOT_CLIENT_ID}
      - BOT_TOKEN=${BOT_TOKEN}
      - LAVALINK_PASSWORD=${LAVALINK_PASSWORD}
      - MISTRAL_API_KEY=${MISTRAL_API_KEY}
    restart: unless-stopped
    networks:
      - lavalink

  lavalink:
    image: ghcr.io/lavalink-devs/lavalink:4-alpine
    container_name: lavalink
    restart: unless-stopped
    environment:
      - LAVALINK_SERVER_PASSWORD=${LAVALINK_PASSWORD}
    volumes:
      - /mnt/user/appdata/lavalink/application.yml:/opt/Lavalink/application.yml
    networks:
      - lavalink
    expose:
      - 2333

networks:
  lavalink:
    name: lavalink
```

## Building

```sh
# Build only
./gradlew build

# Build a distributable archive
./gradlew app:assembleDist

# Run all checks
./gradlew check

# Clean build outputs
./gradlew clean
```

A Lavalink server is provided for development purposes in the `lavalink` directory.

## Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Kord](https://github.com/kordlib/kord) as the Discord API client
- [LavaKord](https://github.com/DRSchlaubi/lavakord) + [Lavalink](https://github.com/lavalink-devs/Lavalink)
  for Discord audio playback
- [LangChain4j](https://github.com/langchain4j/langchain4j) for AI commentary
