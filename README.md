# Limitless Mod

Welcome to **Limitless**, a Minecraft mod designed to redefine your creative experience! This mod adds a dynamic creative inventory tab that updates in real-time with unique items seen across players' equipment. Explore limitless possibilities and keep your creativity flowing!

---

## Features

- **Dynamic Creative Tab**:  
  A new creative inventory tab named **"§9Limitless"** dynamically displays unique items equipped by players.

- **Real-Time Updates**:  
  Items are added to the tab as players equip them, ensuring that the tab stays fresh and updated.

- **No Duplicates**:  
  Uses advanced tracking to prevent duplicate items from cluttering the creative tab.

- **Creative Tab Refresh**:  
  Automatically refreshes the creative inventory when new items are added, providing a seamless experience.

---

## How It Works

1. **Tracking Equipped Items**:  
   The mod listens for equipment updates (`EntityEquipmentUpdateS2CPacket`) and adds unique equipped items to the `Limitless` creative tab.

2. **Preventing Duplicates**:  
   Items are identified using a combination of their type and NBT data, ensuring only unique items are displayed.

3. **Creative Tab Integration**:  
   When new items are detected, the creative tab refreshes automatically to reflect the updates.

---

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/).
2. Install the [Fabric API](https://modrinth.com/mod/fabric-api) if not already present.
3. Place the `Limitless` mod JAR file into your `mods` folder.
4. Launch Minecraft and enjoy the new creative inventory tab!

---

## Usage

- Equip items as usual in your Minecraft world.  
- Open the **Creative Inventory** and navigate to the **Limitless** tab to see unique equipped items.

> **Note**: Items without any equipped instances will display a placeholder (Barrier block).

---

## Developer Information

### Code Structure

- **Main Mod Class**:  
  `Limitless` handles item group registration and item tracking.

- **Client Mod Class**:  
  `LimitlessClient` initializes client-specific logic.

- **Mixin**:  
  `ClientPlayNetworkHandlerMixin` intercepts network packets to track equipped items in real-time.

---

## Contributing

We welcome contributions! If you have ideas, bug reports, or want to contribute code:
1. Fork this repository.
2. Create a new branch for your feature or bugfix.
3. Submit a pull request with a clear description of your changes.

---

## License

This mod is distributed under the [MIT License](LICENSE). Feel free to modify and distribute it, but please give credit to the original authors.

---

## Contact

For questions or support, feel free to reach out:
-
