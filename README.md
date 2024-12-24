# Limitless Mod

![Limitless Logo](https://cdn.modrinth.com/data/cached_images/865647713e4691ffc4400e9d38ce492c2c58e44b.png)

**Limitless** redefines creativity in Minecraft with a dynamic creative inventory tab that updates in real-time based on players' equipped items. Explore endless possibilities with a seamless creative experience!

---

## Features

- **Dynamic Tab**: A new creative inventory tab, **"§9Limitless"**, updates dynamically with unique equipped items.
- **Real-Time Updates**: Automatically adds items as players equip them.
- **No Duplicates**: Tracks items by type and NBT data to prevent duplicates.
- **Seamless Refresh**: Updates the creative inventory automatically.

---

## How It Works

1. **Tracking Items**: Monitors equipped items via `EntityEquipmentUpdateS2CPacket`.
2. **Preventing Duplicates**: Ensures unique item display using type and NBT data.
3. **Tab Refresh**: Refreshes automatically when new items are detected.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Add the `Limitless` mod JAR to your `mods` folder.
3. Launch Minecraft and explore the new creative tab!

---

## Usage

- Equip items as usual.  
- Access the **Limitless** tab in Creative Inventory to view unique equipped items.  
> **Note**: If there have not been any items equipped, the tab will only contain a barrier block.

---

## Developer Info

- **Main Class**: `Limitless` for item group registration and tracking.
- **Client Logic**: `LimitlessClient` handles client-side functionality.
- **Mixin**: `ClientPlayNetworkHandlerMixin` tracks equipment in real-time.

---

## Contributing

Contributions are welcome!  
1. Fork the repo.  
2. Create a feature/bugfix branch.  
3. Submit a pull request with a description.

---

## License

Licensed under the [Apache License 2.0](LICENSE). You may modify and distribute the mod while crediting the original authors.

---

## Contact

For questions or support, reach out via GitHub.
