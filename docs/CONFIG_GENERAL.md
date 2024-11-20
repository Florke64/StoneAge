# General Configuration
version 2

---

This document describes the structure of the `config.yml` file,
main configuration file for the plugin.

Table of contents:

- [Database configuration](#Database) - *SQLite or MySQL*
- [Stone Machines](#Machines) - *Cooldown settings, optional features and more*
- [Drop Resources](#Resources) - *Custom drops, and drop resources spawning*
- [Default configuration](#Default-configuration) - *Example `config.yml` file*

# Database

Default database connector used is SQLite. You can change that to MySQL and enter credentials.

## SQLite or MySQL Connection
`database.connector` ("sqlite" or "mysql"), default: `sqlite`
- Declares preferred connector to be used by plugin.


If you want to use `mysql`, must fill out the following fields:

`database.server_address`, default: `localhost` (127.0.0.1)

`database.server_port`, default: `3306`

`database.db_name`, default: `stoneage`

`database.db_user`, default: `root`

`database.db_password`, default: *none*

## Database security features

`database.db_ssl`, default: `true`
- Enables SSL connection to the database and some other security features. Highly recommended to be `true`. 

# Machines

| Config Entry                       | Default          | Description                                                                                    |
|------------------------------------|------------------|------------------------------------------------------------------------------------------------| 
| `machines.destroy_tool`            | `GOLDEN_PICKAXE` | Tool that is needed to destroy stone machines                                                  |
| `machines.default_drop_multiplier` | `1.0`            | Default drop multiplier for all stone machines                                                 |
| `machines.max_drop_multiplier`     | `5.0`            | Maximum drop multiplier, this limits a multiplier that can be set with a `/multiplier` command |
| `machines.max_miner_level`         | `99`             | Maximum miner level for stone machines                                                         |
| `machines.commands_cooldown`       | `3`              | Cooldown for plugin commands, in seconds                                                       |
| `machines.repair_cooldown`         | `5`              | Cooldown for repair option, available in Stone Machine's GUI, in seconds                       |
| `machines.stone_frequency`         | `40`             | Number in ticks (20 = 1 second), resources respawn rate                                        |
| `machines.drop_exp_to_feet`        | `true`           | Makes stone machines to spawn exp orbs under players feet                                      |
| `machines.drop_items_to_feet`      | `true`           | Makes stone machines to spawn items under players feet                                         |
| `machines.allow_hopper_output`     | `true`           | Redirect stone machine drops to hopper under it                                                |
| `machines.allow_hopper_input`      | `true`           | Allow upgrading stone machines with hopper input (Coal)                                        |

> **Drop Multiplier:** `stoneage.admin` permission is required to use admin command `/multiplier`.
> Drop Multiplier is a feature that multiplies all custom drop rates for a limited time.

# Resources

Resources section is customizable and interpreted later in plugin.
This part defines what *custom drops* loot from *resources*.

**Drop Resources** are blocks that are spawned from Stone Machines, defined in `drops/resources/` directory.
Rest of custom drops are defined in `drops/` directory. You refer to these, so called, Drop Entries by filename name (without extension).

**Drop Entries** are defined in `drops/` directory and linked with Drop Resources via `config.yml`'s `resources` section.
Learn more about [Drop Entry](DROP_ENTRY.md) to understand how to spawn own blocks and define custom drops of them.

## Example

Perquisites: `drops/coal.yml`, `drops/gold.yml`, `drops/quartz.yml`, `drops/resources/stone.yml`
(See: [Drop Entry](DROP_ENTRY.md) documentation).

```yaml
resources:
  NETHERRACK:
    - coal
    - gold
    - quartz
```

> This example will make Stone Machines spawn Netherrack blocks and drop coal, gold and quartz from it.

# Default configuration

Default configuration files are generated on plugin launch if config files or drops directory are missing.
Most of the special plugin features are enabled in this configuration.
Drop rate weights for default Drop Entries are roughly based on vanilla game balance.
