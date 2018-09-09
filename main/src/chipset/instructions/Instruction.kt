package chipset.instructions

import Chip8

interface Instruction {
  fun doOperation(chip: Chip8, instruction: Int)
}