# Plan: Fix Wolf and Skeleton Facing Direction in Battle

## Problem

In the battle screen ([`FightScreen.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/FightScreen.kt)), **wolf and skeleton enemies** on the right side of the screen are facing **right** (away from the player) instead of facing **left** toward the player.

## Root Cause

### Current Rendering Logic

In [`FightScreen.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/FightScreen.kt:378-382), the `TestCharacter` composable applies a horizontal flip:

```kotlin
.graphicsLayer {
    scaleX = if (flipCharacter) -1f else 1f
}
```

- **Players** (left side): `flipCharacter = false` → `scaleX = 1f` (no flip, natural direction)
- **Enemies** (right side): `flipCharacter = true` → `scaleX = -1f` (flipped horizontally)

This logic assumes ALL enemy sprites naturally face right, so flipping them makes them face left toward the player. However, this is only true for the **knight** sprite.

### The Issue

The sprite sheet images have different natural facing directions:

| Sprite | Source File | Natural Direction | Current Treatment | Result |
|--------|------------|-------------------|-------------------|--------|
| Knight (`knight_sprite.webp`) | `shared/.../knight_sprite.webp` | Facing **right** | Player: no flip → faces right ✅ | Correct |
| Skeleton (`skeleton_idle.png`) | `shared/.../skeleton_idle.png` | Facing **left** | Enemy: flipped → faces right ❌ | Wrong |
| Wolf (`wolf_idle.png`) | `shared/.../wolf_idle.png` | Facing **left** | Enemy: flipped → faces right ❌ | Wrong |

The wolf and skeleton sprites naturally face **left** in their source PNG files. When placed on the right (enemy) side and flipped with `scaleX = -1f`, they end up facing **right** (away from the player), which is incorrect.

## Solution

Instead of a blanket `flipCharacters` flag for the entire enemy team, we need per-character control over sprite flipping. Characters whose sprites naturally face left (wolf, skeleton) should NOT be flipped even when on the enemy side.

### Changes Required

#### 1. [`CharacterUiModel.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/character/CharacterUiModel.kt) - Add `flipSprite` field

Add a `flipSprite: Boolean = false` property to `CharacterUiModel`. This indicates whether the sprite should be horizontally flipped when rendered.

#### 2. [`GameViewModel.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/GameViewModel.kt) - Set `flipSprite` in `toUiModel()`

In the `CharacterTemplate.toUiModel()` method (line ~608), set `flipSprite` based on the avatar type. The knight sprite faces right naturally, so it needs flipping when on the enemy side. Wolf and skeleton face left naturally, so they don't need flipping.

The logic should be:
- `flipSprite = (avatar == "knight")` — only the knight sprite needs flipping when on the enemy side

Wait — actually, the current `flipCharacter` parameter is applied to ALL enemies. The knight is a player character, not an enemy. So the knight is never on the enemy side with the current code.

Let me reconsider: The `flipCharacter` parameter is `true` for the enemy team (wolves and skeletons). Since wolf/skeleton sprites face left naturally, and they're on the right side, they should NOT be flipped. So `flipSprite` should be `false` for wolf and skeleton.

But what about the knight player? The knight faces right naturally and is on the left side without flipping — that's correct.

So the fix is simpler: **wolf and skeleton characters should have `flipSprite = false`** (they face left naturally, which is toward the player when on the right side).

Actually, let me reconsider more carefully. The `flipCharacter` parameter is passed from `TeamFormation` to `CharacterSlot` to the `graphicsLayer`. Currently:
- Player team: `flipCharacters = false` → `scaleX = 1f` → natural direction
- Enemy team: `flipCharacters = true` → `scaleX = -1f` → flipped

Since wolf/skeleton face left naturally, and they're enemies on the right side:
- If NOT flipped (`scaleX = 1f`): they face left → toward the player ✅
- If flipped (`scaleX = -1f`): they face right → away from the player ❌

So the fix is: **don't flip wolf and skeleton sprites**. The simplest approach is to make the flip decision per-character rather than per-team.

#### 3. [`FightScreen.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/FightScreen.kt) - Use per-character flip logic

Change the `graphicsLayer` to use `character.flipSprite` instead of the team-level `flipCharacter` parameter.

### Detailed Code Changes

#### File 1: `CharacterUiModel.kt`

Add `flipSprite` field:
```kotlin
data class CharacterUiModel(
    val id: String,
    val name: String = id,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val mana: Int,
    val isPlayer: Boolean,
    val sprites: CharacterSprites,
    val state: CharacterState = CharacterState.IDLE,
    val currentTargetId: String? = null,
    val flipSprite: Boolean = false  // NEW
)
```

#### File 2: `GameViewModel.kt`

In `CharacterTemplate.toUiModel()` (~line 608), set `flipSprite`:
```kotlin
private fun CharacterTemplate.toUiModel(): CharacterUiModel =
    CharacterUiModel(
        id = id,
        name = name,
        hp = hp,
        maxHp = maxHp,
        attack = attack,
        mana = mana,
        isPlayer = isPlayer,
        sprites = mapCharacterSprites(avatar),
        flipSprite = avatar == "knight"  // Knight faces right, needs flip when on enemy side
    )
```

Wait — the knight is always a player (left side), so it never gets flipped. Let me think again...

Actually, the simplest correct approach: **wolf and skeleton sprites face left naturally. When on the right (enemy) side, they should NOT be flipped.** So:

```kotlin
flipSprite = avatar in setOf("wolf", "skeleton")  // These face left naturally
```

No wait, that's backwards. If `flipSprite = true` means "flip this sprite", and we currently flip ALL enemies, then we want to NOT flip wolf/skeleton. So:

```kotlin
// In toUiModel - default flipSprite to false
// The flip will be applied in FightScreen based on team side + character flipSprite
```

Let me reconsider the architecture. The cleanest approach:

**In `CharacterUiModel`**: Add `flipSprite` — whether this specific character's sprite needs to be flipped when rendered.

**In `FightScreen.kt`**: Change from team-level flip to character-level flip. The `flipCharacter` parameter on `TeamFormation` is removed. Instead, each `CharacterSlot` checks `character.flipSprite`.

**In `GameViewModel.kt`**: Set `flipSprite` based on the character's natural facing direction and which side they're on.

The logic for `flipSprite`:
- Characters on the **left side** (players) should face **right** (toward enemies)
- Characters on the **right side** (enemies) should face **left** (toward players)
- Knight sprite faces right naturally → on left side: no flip ✅, on right side: flip needed
- Wolf/skeleton face left naturally → on left side: flip needed, on right side: no flip ✅

Since wolf/skeleton are always enemies (right side) and knight is always a player (left side):
- Knight: `flipSprite = false` (already facing right on left side)
- Wolf/Skeleton: `flipSprite = false` (already facing left on right side)

So **all characters should have `flipSprite = false`** with the current team assignments! The bug is that the enemy team blanket-flip (`flipCharacters = true`) incorrectly flips wolf/skeleton.

**The fix**: Simply remove the `flipCharacters = true` for enemies, OR make it so the flip is applied per-character based on their sprite's natural direction.

### Final, Cleanest Solution

Since the `flipCharacter` parameter on `TeamFormation` is a team-level concept, and we need per-character control, let's:

1. Remove the `flipCharacters` parameter from `TeamFormation` and `CharacterSlot`
2. Add `flipSprite` to `CharacterUiModel`
3. Set `flipSprite` in `GameViewModel.toUiModel()` based on what makes each character face the correct direction
4. Use `character.flipSprite` in the `graphicsLayer`

For the current setup (knight on left, wolf/skeleton on right):
- Knight faces right naturally, is on left → should face right → `flipSprite = false`
- Wolf/Skeleton face left naturally, are on right → should face left → `flipSprite = false`

So `flipSprite` would be `false` for all current characters. But this is more future-proof if characters change sides.

## Files to Modify

1. [`feature/game/src/commonMain/kotlin/com/lernhero/game/character/CharacterUiModel.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/character/CharacterUiModel.kt) — Add `flipSprite: Boolean = false`
2. [`feature/game/src/commonMain/kotlin/com/lernhero/game/GameViewModel.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/GameViewModel.kt) — Set `flipSprite` in `toUiModel()` (currently all `false`, but explicit is better)
3. [`feature/game/src/commonMain/kotlin/com/lernhero/game/FightScreen.kt`](feature/game/src/commonMain/kotlin/com/lernhero/game/FightScreen.kt) — 
   - Remove `flipCharacters` parameter from `TeamFormation` and `CharacterSlot`
   - Change `graphicsLayer { scaleX = if (flipCharacter) -1f else 1f }` to `graphicsLayer { scaleX = if (character.flipSprite) -1f else 1f }`

## Testing

- ✅ Knight (player, left side) faces right toward enemies
- ✅ Skeleton (enemy, right side) faces left toward player
- ✅ Wolf (enemy, right side) faces left toward player
- ✅ Attack movement animation: characters move toward their target
- ✅ Hit/dead states show correct facing direction
