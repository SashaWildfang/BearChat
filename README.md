# BearChat

The chat core for the **Bear Nation** Minecraft network: formatted global chat, chat channels, private messages, offline mail, and moderation tools in one plugin.

![Paper](https://img.shields.io/badge/Paper-26.1.2-blue) ![Java](https://img.shields.io/badge/Java-25-orange) ![Build](https://img.shields.io/badge/build-Gradle-02303A)

## Features

- **Chat channels**: switch between global and staff channels, or send a single message to staff chat
- **Private messaging**: `/msg` and `/reply`, with many familiar aliases (`/tell`, `/w`, `/whisper`, `/r`)
- **Ignore system**: block messages from specific players and manage your ignore list
- **Offline mail**: leave messages for players who aren't online
- **Moderation**: clear chat, lock/unlock chat, and social spy for staff
- **PlaceholderAPI** support in chat formats
- Every message is configurable in `config.yml`

## Commands

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/msg <player> <message>` | `tell`, `message`, `m`, `w`, `whisper` | Send a private message | – |
| `/reply <message>` | `r` | Reply to your last conversation | – |
| `/ignore <player>` | – | Ignore a player | – |
| `/ignorelist` | – | Manage ignored players | – |
| `/channel <name>` | `chat` | Switch chat channels | – |
| `/mail` | – | Send and read offline mail | – |
| `/staffchat [message]` | `sc` | Toggle or send to staff chat | `bn.channel.staff` |
| `/socialspy` | – | See private messages between players | `bn.bearchat.socialspy` |
| `/clearchat` | `cc` | Clear global chat | `bn.bearchat.admin` |
| `/chatadmin` | `lockchat`, `unlockchat` | Manage chat settings | `bn.bearchat.admin` |

## Requirements

- Paper 26.1.2+
- Java 25
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (required)

## Building

```bash
./gradlew build
```

The plugin jar goes to `build/libs/`. Running `./gradlew runServer` starts a local Paper test server with the plugin loaded.

## Project layout

```
src/main/java/com/sasha/bearchat/
├── BearChat.java        # Plugin entry point
├── commands/            # Command executors and tab completion
├── listeners/           # Chat event handling
└── managers/            # Channel, ignore and mail state
```

---

Part of the Bear Nation plugin suite.
