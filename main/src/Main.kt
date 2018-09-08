class Runner(private val chip: Chip8, private val animator: IAnimationWrapper) {
  fun run(game: String) {
    // Set up render system and register input callbacks
    animator.setupGraphics()
    animator.setupInput()

    // Initialize the Chip7 system and load the game into memory
    chip.initialize()
    chip.loadGame(game)

    emulationLoop()
  }

  private fun emulationLoop() {
    while(true) {
      // emulate one CPU cycle
      chip.emulateCycle()

      // If the draw flag is set, update the screen
      if(chip.drawFlag != null) animator.drawGraphics()

      // Store key press state (Press and Release)
      chip.setKeys()
    }
  }
}