# StoneAge

![StoneAge Banner](image/banner.png)

Paper Plugin adding stone generator machines. With custom drops and cool features.

- Can be placed in weird directions
- Can auto-smelt drops when powered with coal (via hoppers)
- Can pick up drops with hopper or throw them directly at player
- Can spawn different resource blocks which drop different items
- *... and more (, and more coming in the future!)!1*

Custom config, language file, two database connectors, admin "event" command is also there.

![[Drop Filter]](image/drop_filter.png)
`drop_filter.png`: drop filter

### Latest Release

> **Note:** This plugin is still in its development stage!

The latest release details can be seen [here](https://github.com/FlrQue/StoneAge/releases/latest)
(what's new and *.jar downloads).

![[Machine]](image/machine_upfacing.png)
`machine_upfacing.png`: stone machine placed, facing up

~~API and Configuration is still subject to change!~~

### Configuration

> See: [General Configuration](docs/CONFIG_GENERAL.md)

Plugin configuration is based on `config.yml`, where you can setup database connection
and enable some plugin's unique features like auto-smelting etc.

Config also defines Drop Resource to Custom Drops relationship.

You will put [Drop Entries](docs/DROP_ENTRY.md) and Drop Resources in `drops/` and `drops/resources/` directories respectively.
These define types of blocks to spawn and their custom drops.
Drop chance weights and other parameters, like resistance to Fortune Enchantment or Silk Touch drop, are defined in respective Drop(/Resource) Entries.

![[Drop Actions]](image/machine_actions.png)
`drop_actions.png`: available actions in Machine's GUI

Learn more about [General Configuration](docs/CONFIG_GENERAL.md) from docs.

### Permissions

Currently only `stoneage.admin` & `stoneage.drop` permissions are available.

- `stoneage.admin` permission is required to use `/multiplier` command which highers a drop rate.

- `stoneage.drop` permission is required to use simple `/drop` command for player configuration.

### API

An API usage will be explained later (if even ever needed).
Most important stuff is that plugin initializes StoneMachine object which contains a lot of useful stuff.

For instance, it is a gateway to `ResourceSpawner`, `DropCalculator`, etc.
Also, check the `pl.florke.stoneage.event` package.

### Known issues

List of all known issues can be
seen [here](https://github.com/FlrQue/StoneAge/issues?q=is%3Aissue+label%3A%22known+issue%22+is%3Aopen "See known issues"). 

### License (GNU GPL v3)

> Copyright (c) 2024. @Florke64 <Daniel Chojnacki>
>
> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation, either version 3 of the License, or
> (at your option) any later version.
>
> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
> GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License
> along with this program.  If not, see <http://www.gnu.org/licenses/>.

### Origin story

> Plugin was originally developed by @Florke64 for a _public skyblock server project_,
> that was active at the time and with a group of friends met online. As I left the project, plugin was abandoned.
> At the moment, nobody of us is using it anymore; As a code owner, I decided to update licensing to GPLv3, then publish the code.
> Legacy versions used directly on Go2Play.pl server are available in `g2p-legacy` and `g2p-legacy-prerelease` branches.
> You can study code, however I don't give permission to use old legacy versions of this plugin as it was exclusive for Go2Play at the time.
> Please remember to read the license.
 
![StoneAge Logo](image/logo128.png)