# TrueTap Design System Implementation Guide

## 🎯 Overview

This guide outlines the comprehensive design system for TrueTap to ensure consistent UI/UX across all screens. The system includes standardized layouts, components, spacing, typography, and colors.

## 📋 Design Principles

### 1. **Consistency First**
- All screens should follow the same layout patterns
- Use standardized components instead of custom implementations
- Maintain consistent spacing, typography, and colors

### 2. **Accessibility**
- Support for large text mode
- High contrast mode support
- Proper touch targets (minimum 44dp)
- Screen reader compatibility

### 3. **Mobile-First**
- Optimized for mobile interaction patterns
- Adaptive layouts for different screen sizes
- System navigation bar awareness

### 4. **Performance**
- Reusable components to reduce code duplication
- Efficient rendering with proper state management
- Minimal unnecessary recompositions

## 🏗️ Architecture

### Core Components

```
ui/
├── components/
│   ├── layouts/
│   │   └── ScreenTemplates.kt          # Standard screen layouts
│   ├── StandardComponents.kt           # Reusable UI components
│   └── BottomNavigation.kt            # Navigation component
├── theme/
│   ├── TrueTapDesignSystem.kt         # Colors, typography, spacing
│   └── Theme.kt                       # Material3 theme integration
└── accessibility/
    └── AccessibilitySettings.kt       # Accessibility configurations
```

## 📐 Layout System

### Screen Templates

Use these standardized templates instead of creating custom layouts:

#### 1. **TrueTapScreenTemplate** - Standard screens with bottom navigation
```kotlin
TrueTapScreenTemplate(
    title = "Screen Title",
    currentTab = BottomNavItem.HOME,
    subtitle = "Optional subtitle",
    onNavigateToHome = { /* navigation */ },
    // ... other navigation handlers
    actions = {
        TrueTapButton(
            text = "Action",
            onClick = { /* action */ },
            style = TrueTapButtonStyle.ICON
        )
    },
    floatingActionButton = {
        TrueTapButton(
            text = "Add",
            onClick = { /* action */ },
            icon = Icons.Default.Add
        )
    }
) {
    // Content using LazyListScope
    item {
        TrueTapCard {
            Text("Content goes here")
        }
    }
}
```

#### 2. **TrueTapSearchableScreenTemplate** - Screens with search functionality
```kotlin
TrueTapSearchableScreenTemplate(
    title = "Contacts",
    currentTab = BottomNavItem.CONTACTS,
    searchQuery = searchQuery,
    onSearchQueryChange = { searchQuery = it },
    searchPlaceholder = "Search contacts...",
    // ... navigation handlers
) {
    // Searchable content
}
```

#### 3. **TrueTapModalScreenTemplate** - Modal/detail screens
```kotlin
TrueTapModalScreenTemplate(
    title = "Detail View",
    onNavigateBack = { /* back navigation */ },
    subtitle = "Additional info"
) {
    // Modal content
}
```

## 🎨 Component Library

### Cards
```kotlin
// Standard card
TrueTapCard {
    Text("Card content")
}

// Elevated card for important content
TrueTapCard(style = TrueTapCardStyle.ELEVATED) {
    Text("Important content")
}

// Clickable card
TrueTapCard(onClick = { /* action */ }) {
    Text("Clickable content")
}
```

### Buttons
```kotlin
// Primary action button
TrueTapButton(
    text = "Primary Action",
    onClick = { /* action */ },
    style = TrueTapButtonStyle.PRIMARY
)

// Secondary button
TrueTapButton(
    text = "Secondary",
    onClick = { /* action */ },
    style = TrueTapButtonStyle.SECONDARY
)

// Button with icon
TrueTapButton(
    text = "Save",
    onClick = { /* action */ },
    icon = Icons.Default.Save,
    loading = isLoading
)
```

### List Items
```kotlin
TrueTapListItem(
    title = "Item Title",
    subtitle = "Optional subtitle",
    leadingIcon = Icons.Default.Person,
    onClick = { /* action */ },
    trailingContent = {
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
)
```

### Section Headers
```kotlin
TrueTapSectionHeader(
    title = "Section Title",
    subtitle = "Optional description",
    action = {
        TrueTapButton(
            text = "See All",
            onClick = { /* action */ },
            style = TrueTapButtonStyle.TEXT
        )
    }
)
```

### Empty States
```kotlin
TrueTapEmptyState(
    title = "No Items Found",
    description = "Add some items to get started",
    icon = Icons.Default.Inbox,
    action = {
        TrueTapButton(
            text = "Add Item",
            onClick = { /* action */ }
        )
    }
)
```

### Loading States
```kotlin
TrueTapLoadingState(
    message = "Loading your data..."
)
```

## 🎯 Spacing System

Use the standardized spacing values:

```kotlin
object TrueTapSpacing {
    val xs = 4.dp      // Micro spacing
    val sm = 8.dp      // Small spacing  
    val md = 16.dp     // Medium spacing
    val lg = 24.dp     // Large spacing
    val xl = 32.dp     // Extra large spacing
    val xxl = 48.dp    // Screen margins
}
```

### Usage Examples:
```kotlin
// Consistent padding
Modifier.padding(TrueTapSpacing.lg)

// Consistent spacing between elements
Arrangement.spacedBy(TrueTapSpacing.md)

// Consistent margins
Modifier.padding(horizontal = TrueTapSpacing.lg, vertical = TrueTapSpacing.md)
```

## 📝 Typography System

Use the standardized typography scale:

```kotlin
// Get dynamic typography (adjusts for accessibility)
val typography = getDynamicTypography(accessibility.largeButtonMode)

// Usage
Text(
    text = "Headline",
    style = typography.headlineLarge,
    color = dynamicColors.textPrimary
)
```

### Typography Scale:
- **Display**: 57sp, 45sp, 36sp - Hero text, large displays
- **Headline**: 32sp, 28sp, 24sp - Section headers, page titles  
- **Title**: 22sp, 16sp, 14sp - Card titles, list items
- **Body**: 16sp, 14sp, 12sp - Body text, descriptions
- **Label**: 14sp, 12sp, 11sp - Labels, captions

## 🎨 Color System

Always use dynamic colors that adapt to theme and accessibility settings:

```kotlin
val accessibility = LocalAccessibilitySettings.current
val dynamicColors = getDynamicColors(
    themeMode = accessibility.themeMode,
    highContrastMode = accessibility.highContrastMode
)

// Usage
Text(
    text = "Primary text",
    color = dynamicColors.textPrimary
)

Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = dynamicColors.primary,
        contentColor = dynamicColors.onPrimary
    )
)
```

### Color Categories:
- **Primary**: Brand colors (orange theme)
- **Text**: Primary, secondary, tertiary, inactive
- **Status**: Success, error, warning, info
- **Background**: Main background, surface, variants
- **Outline**: Borders and dividers

## 🔄 Migration Guide

### Step 1: Replace Custom Layouts
**Before:**
```kotlin
@Composable
fun CustomScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom header
        Text("Title", fontSize = 28.sp)
        // Custom content
        LazyColumn { ... }
        // Custom bottom nav
        CustomBottomNav()
    }
}
```

**After:**
```kotlin
@Composable
fun CustomScreen() {
    TrueTapScreenTemplate(
        title = "Title",
        currentTab = BottomNavItem.HOME,
        onNavigateToHome = { /* navigation */ }
    ) {
        // Content items
        item { 
            TrueTapCard { /* content */ }
        }
    }
}
```

### Step 2: Replace Custom Components
**Before:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Title", fontWeight = FontWeight.Bold)
        Text("Subtitle", color = Color.Gray)
    }
}
```

**After:**
```kotlin
TrueTapCard {
    TrueTapListItem(
        title = "Title",
        subtitle = "Subtitle"
    )
}
```

### Step 3: Update Spacing and Typography
**Before:**
```kotlin
Text(
    text = "Title",
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(20.dp)
)
```

**After:**
```kotlin
Text(
    text = "Title",
    style = getDynamicTypography().headlineSmall,
    color = getDynamicColors().textPrimary,
    modifier = Modifier.padding(TrueTapSpacing.lg)
)
```

## ✅ Implementation Checklist

### For Each Screen:
- [ ] Uses appropriate screen template
- [ ] Replaces custom components with standard ones
- [ ] Uses TrueTapSpacing for all spacing
- [ ] Uses getDynamicColors() for all colors
- [ ] Uses getDynamicTypography() for all text
- [ ] Implements proper accessibility support
- [ ] Uses shared bottom navigation component
- [ ] Follows consistent content organization patterns

### Screen-Specific Guidelines:

#### Home Screen:
- Use `TrueTapScreenTemplate`
- Feature cards use `TrueTapCard` with `ELEVATED` style
- Quick actions use `TrueTapButton` grid
- Balance overview uses consistent typography

#### Settings Screen:
- Use `TrueTapScreenTemplate`
- Settings sections use `TrueTapSectionHeader`
- Settings items use `TrueTapListItem`
- Modal dialogs use standard Material3 components

#### NFTs Screen:
- Use `TrueTapScreenTemplate` for main view
- Use `TrueTapModalScreenTemplate` for detail view
- NFT cards use `TrueTapCard`
- Empty state uses `TrueTapEmptyState`

#### Contacts Screen:
- Use `TrueTapSearchableScreenTemplate`
- Contact items use `TrueTapListItem`
- Avatars use `TrueTapAvatar`
- FAB uses `TrueTapButton`

#### Swap Screen:
- Use `TrueTapScreenTemplate`
- Form inputs use standard Material3 components
- Action buttons use `TrueTapButton`
- Status displays use appropriate colors

## 🚀 Benefits

### For Developers:
- Faster development with pre-built components
- Consistent code patterns across the team
- Easier maintenance and updates
- Better accessibility by default

### For Users:
- Consistent experience across all screens
- Better accessibility support
- Improved usability with standard patterns
- Cohesive visual identity

### For Design:
- Systematic approach to UI design
- Easy to maintain design consistency
- Scalable design system for future features
- Clear documentation for design decisions

## 📚 Best Practices

1. **Always use screen templates** - Don't create custom layouts
2. **Prefer standard components** - Only create custom components when absolutely necessary
3. **Use dynamic colors and typography** - Support theming and accessibility
4. **Follow spacing system** - Use TrueTapSpacing values consistently
5. **Test accessibility** - Verify large text mode and high contrast mode
6. **Document deviations** - If you must deviate from the system, document why
7. **Review before merge** - Ensure consistency in code reviews

## 🔧 Development Workflow

1. **Design Review**: Ensure design follows system guidelines
2. **Component Selection**: Choose appropriate template and components
3. **Implementation**: Build using standard components
4. **Accessibility Testing**: Test with different accessibility settings
5. **Code Review**: Verify consistency with design system
6. **QA Testing**: Test across different devices and themes

This design system ensures that TrueTap maintains a consistent, accessible, and professional user experience across all screens while making development more efficient and maintainable.