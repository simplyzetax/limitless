# Limitless Mod

![Limitless Logo](https://cdn.modrinth.com/data/cached_images/865647713e4691ffc4400e9d38ce492c2c58e44b.png)

**Limitless** is a fun Minecraft mod where I'm adding whatever features I think would be cool or useful! It started as a dynamic creative inventory mod but has grown into a collection of quality-of-life features and interesting mechanics. This is an experimental playground where I implement features that seem fun - suggestions are always welcome!

---

## Features

### 🎒 Dynamic Creative Inventory
- **Limitless Tab**: A new creative inventory tab that updates dynamically with unique equipped items
- **Real-Time Updates**: Automatically adds items as players equip them
- **Item Stealing**: "Steal" items from other players or entities by interacting with them
- **No Duplicates**: Smart tracking prevents duplicate items in the inventory

### 📦 Shulker Box Integration  
- **Shulker Box Tab**: Dedicated creative tab for captured shulker box contents
- **Auto-Capture**: Automatically captures shulker box contents when you interact with them
- **Content Preservation**: Maintains all items and their NBT data from shulker boxes

### ✨ Entity Glowing
- **Player Glowing**: Make players glow with colored team indicators
- **Threat Detection**: Arrows that pose a threat to you glow red automatically
- **Visual Enhancements**: Better visibility for important entities

### 🏹 Bow Features
- **Trajectory Visualization**: See the predicted path of your arrows when aiming
- **Arrow Dodge System**: Automatically dodge incoming arrows (when enabled)
- **Threat Analysis**: Advanced arrow threat detection with path obstruction checking

### 💥 Damage Numbers
- **Visual Damage**: Floating damage numbers appear when entities take damage
- **Multiple Types**: Different colors for normal, critical, healing, and environmental damage
- **Customizable Fonts**: Choose from different font styles for damage display

### 🔍 Zoom Feature
- **Dynamic Zoom**: Smooth zoom functionality similar to OptiFine
- **Configurable Levels**: Adjustable zoom sensitivity and range
- **Mouse Sensitivity**: Smart mouse sensitivity scaling for precision

### ⚙️ Settings GUI
- **In-Game Configuration**: Easy-to-use settings interface
- **Feature Toggles**: Enable/disable any feature on the fly
- **Real-Time Updates**: Changes apply immediately without restart

---

## Philosophy

This mod is all about experimentation and fun! I add features that:
- Solve quality-of-life issues I encounter while playing
- Implement cool mechanics I think would be interesting
- Provide useful tools for creative and survival gameplay
- Are technically challenging and fun to code
- A personal one, but some people may not like this: I am using a lot of AI generated code in this because I am just starting to learn proper Java. I always try to learn what it writes for me when I use it, but I want to be honest about this

**Got an idea?** Feel free to suggest features! This is a playground mod where anything goes.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api)
2. Add the `Limitless` mod JAR to your `mods` folder
3. Launch Minecraft and start exploring the features!

---

## Usage

### Creative Inventory Features
- **Limitless Tab**: Access unique equipped items from all players
- **Shulker Box Tab**: Browse captured shulker box contents
- **Item Stealing**: Right-click entities to "steal" their equipped items

### Combat Features  
- **Bow Trajectory**: Hold a bow to see arrow trajectory
- **Arrow Dodge**: Enable auto-dodging in settings (disabled by default)
- **Damage Numbers**: See floating damage numbers on entities

### Utility Features
- **Zoom**: Use configurable zoom keybind (similar to OptiFine)
- **Entity Glowing**: Toggle player/entity glowing effects
- **Settings GUI**: Access in-game configuration panel

> **Note**: Most features are configurable through the in-game settings GUI. Features like arrow dodging are disabled by default for balance.

---

## Configuration

All features can be toggled and configured through:
- In-game settings GUI (recommended)
- Config files in `.minecraft/config/limitless/`
- Client-side configuration options

---

## Technical Details

### Architecture
- **Feature-Based Structure**: Each feature is self-contained with its own mixins and utilities
- **Client-Side Only**: All features work client-side for compatibility
- **Mixin Integration**: Uses Fabric mixins for seamless Minecraft integration
- **Real-Time Updates**: Dynamic systems update without requiring restarts

### Developer Information
- **Main Class**: `Limitless` handles mod initialization
- **Client Logic**: `LimitlessClient` manages feature lifecycle
- **Feature Management**: Modular feature system for easy extension
- **Build System**: Gradle with Fabric toolchain

---

## Contributing & Suggestions

This mod is a fun project where I experiment with Minecraft mechanics! 

**Want to suggest a feature?** 
- Open an issue on GitHub
- Describe what you'd like to see
- Explain why it would be cool/useful

**Contributing:**
- Fork the repository
- Add your feature following the existing structure
- Submit a pull request

---

## License & Credits

This mod is open source and available for learning and experimentation. Feel free to explore the code, suggest improvements, or use it as reference for your own projects!

**Dependencies:**
- Fabric Loader
- Fabric API
- Minecraft (compatible versions listed in mod metadata)

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
