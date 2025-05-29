# Limitless Mod - Code Structure Documentation

## Overview
The Limitless Minecraft mod has been completely reorganized into a feature-based architecture for better maintainability, modularity, and scalability.

## Directory Structure

```
src/client/java/me/simplyzetax/limitless/client/
├── features/
│   ├── core/                          # Core feature management system
│   │   ├── FeatureModule.java         # Base interface for all features
│   │   └── FeatureManager.java        # Manages feature lifecycle
│   ├── itemstealing/                  # Item capturing from entities
│   │   ├── managers/
│   │   │   └── LimitlessItemGroupManager.java
│   │   ├── mixins/
│   │   │   ├── ClientPlayNetworkHandlerMixin.java
│   │   │   ├── CreativeInventoryDropMixin.java
│   │   │   └── CreativeInventorySlotClickMixin.java
│   │   └── util/
│   │       ├── ItemStackDataExtractor.java
│   │       ├── ItemStackDisplayFormatter.java
│   │       ├── ItemStackKeyGenerator.java
│   │       ├── ItemStackMetadataCleaner.java
│   │       ├── ItemStackProcessor.java
│   │       └── OriginalItemStorage.java
│   ├── shulkerboxes/                  # Shulker box content capturing
│   │   ├── managers/
│   │   │   └── ShulkerBoxItemGroupManager.java
│   │   └── mixins/
│   │       ├── ShulkerBoxInteractionMixin.java
│   │       └── ShulkerBoxPlacementMixin.java
│   ├── glowing/                       # Entity glowing with colored teams
│   │   └── mixins/
│   │       ├── EntityGlowMixin.java
│   │       └── PlayerListHudMixin.java
│   ├── bowtrajectory/                 # Bow arrow trajectory visualization
│   │   ├── mixins/
│   │   │   └── BowTrajectoryMixin.java
│   │   └── render/
│   │       ├── BowTrajectoryData.java
│   │       └── BowTrajectoryRenderer.java
│   ├── arrowdodge/                    # Automatic arrow dodging system
│   │   ├── mixins/
│   │   │   ├── ArrowDodgeMixin.java
│   │   │   └── ArrowGlowMixin.java
│   │   └── util/
│   │       ├── ArrowInfo.java
│   │       ├── BowChargeInfo.java
│   │       ├── DodgeCalculation.java
│   │       └── ThreatArrowData.java
│   ├── damagenumbers/                 # Damage number display system
│   │   ├── mixins/
│   │   │   ├── DamageNumberMixin.java
│   │   │   └── DamageNumberRenderMixin.java
│   │   └── util/
│   │       ├── DamageFontManager.java
│   │       ├── DamageNumber.java
│   │       └── DamageNumberManager.java
│   └── gui/                           # GUI and settings system
│       ├── screens/
│       │   └── SettingsGUI.java
│       └── util/
│           └── CreativeScreenManager.java
├── shared/                            # Shared components across features
│   └── config/
│       └── ClientConfig.java          # Global configuration
└── LimitlessClient.java               # Main client initialization
```

## Features

### 1. Item Stealing System
- **Purpose**: Captures items from entities the player interacts with
- **Key Components**:
  - `LimitlessItemGroupManager`: Manages the creative inventory tab for captured items
  - `ItemStackProcessor`: Processes and formats captured items for display
  - Various utility classes for item metadata handling

### 2. Shulker Box Integration
- **Purpose**: Captures shulker box contents when placed or interacted with
- **Key Components**:
  - `ShulkerBoxItemGroupManager`: Manages shulker box specific creative inventory tab
  - Mixins for detecting shulker box interactions and placements

### 3. Entity Glowing
- **Purpose**: Makes players/entities glow with colored team indicators
- **Key Components**:
  - `EntityGlowMixin`: Handles entity glow rendering
  - `PlayerListHudMixin`: Updates player list display

### 4. Bow Trajectory Visualization
- **Purpose**: Shows the trajectory path of arrows when using a bow
- **Key Components**:
  - `BowTrajectoryRenderer`: Renders trajectory lines
  - `BowTrajectoryData`: Stores trajectory calculation data

### 5. Arrow Dodge System
- **Purpose**: Automatically dodges incoming arrows
- **Key Components**:
  - `ArrowDodgeMixin`: Handles automatic dodging logic
  - `DodgeCalculation`: Calculates optimal dodge movements
  - Various utility classes for arrow threat detection

### 6. Damage Numbers
- **Purpose**: Displays damage numbers when entities take damage
- **Key Components**:
  - `DamageNumberManager`: Manages active damage number displays
  - `DamageFontManager`: Handles damage number rendering fonts
  - Mixins for damage detection and rendering

### 7. GUI/Settings System
- **Purpose**: Provides user interface for mod configuration
- **Key Components**:
  - `SettingsGUI`: Main settings screen interface
  - `CreativeScreenManager`: Manages creative inventory interactions

## Feature Management System

The mod now uses a centralized feature management system:

- **`FeatureModule`**: Base interface that all features can implement
- **`FeatureManager`**: Centralized manager for feature lifecycle
  - **Status**: ✅ **Integrated and Active**
  - Handles initialization of all features through `LimitlessClient.onInitializeClient()`
  - Provides error handling and logging for each feature
  - Supports feature enable/disable functionality
  - Manages cleanup when features are unloaded
  - Currently manages: Item Stealing, Shulker Box Integration, and Bow Trajectory features

## Configuration

- **`ClientConfig`**: Centralized configuration in `shared/config/`
- Accessible from all features
- Contains feature-specific settings

## Mixins Configuration

Updated `limitless.client.mixins.json` now uses feature-based package structure:
```json
{
  "package": "me.simplyzetax.limitless.client.features",
  "client": [
    "itemstealing.mixins.ClientPlayNetworkHandlerMixin",
    "glowing.mixins.EntityGlowMixin",
    // ... other mixins organized by feature
  ]
}
```

## Benefits of New Structure

1. **Modularity**: Each feature is self-contained with its own mixins, utilities, and managers
2. **Maintainability**: Clear separation of concerns makes code easier to understand and modify
3. **Scalability**: New features can be added easily following the established pattern
4. **Organization**: Related code is grouped together logically
5. **Testability**: Features can be tested independently
6. **Configuration**: Centralized configuration with feature-specific sections

## Development Guidelines

When adding new features:

1. Create a new directory under `features/`
2. Organize code into appropriate subdirectories (`mixins/`, `util/`, `managers/`, etc.)
3. Implement `FeatureModule` interface if complex initialization is needed
4. Register the feature in `FeatureManager`
5. Update mixins configuration with feature-based package paths
6. Add feature-specific configuration to `ClientConfig` if needed

## Migration Notes

- All old package imports have been updated to use the new structure
- Feature initialization is now handled through `FeatureManager`
- Cross-feature dependencies use proper package imports
- Old directory structure has been cleaned up
