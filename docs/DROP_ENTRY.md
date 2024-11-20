# General Configuration
version 1

---

This document describes the structure of a Drop Entry file.
Drop Entry is a file that defines a drop for a related resources.

> **Note:** Main configuration file for the plugin can be explored [here](docs/CONFIG_GENERAL.md).
> You must set `DropResource` ↔ `DropEntry` relationship in the `config.yml` file.

Table of contents:

- [Drop Rates](#drop-rates) - *Drop Entry structure, drop chance weights, exp etc.*
- [Custom Drops](#custom-drops) - *Drop Entry structure, custom drops definitions*

# Drop Rates

You can set `custom_name` for custom drop title to be displayed in GUI and in chat.
Mining level required for drop is defined by `miner_level` entry. But exp from drop is defined by `miner_exp`.

Percentage chance for drop is represented by `weight`. It is a float number, not limited to 0-1 range.

Respectively, `minimal_exp` and `maximal_exp` define minimum and maximum exp orb drops.

If `ignore_fortune` is set to `true`, drop will not drop more items with `fortune` enchantment.
Same applies for `multipliable`; when set to `true`, drops won't be multiplied by server events.

# Custom Drops

Custom drop consist of `block`, `drop` and `silk` sections. All three are optional and configured the same way.
Drops are represented by Items (ItemStacks) built the following way:

```yaml
material: 'DIAMOND'       # Bukkit Material
display_name: 'Diament!'  # Item's name 
lore:                     # List of lore lines
- '&7Default Drop'        # ...
minimal_amount: 1         # Minimal amount of drops
maximal_amount: 1         # Minimal amount of drops
enchantments:             # List of Applied Enchantments
- 'SILK_TOUCH 1'          # Enchant defined by: EnchantName Level
```

- See: [Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) for all available materials to use
- See: [Enchantments](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html) for all available enchantments
