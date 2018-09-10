package chipset.instructions

import Chip8

class XYN : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val x = (instruction and 0x0F00) shr 8
    val y = (instruction and 0x00F0) shr 4
    val n = instruction and 0x000F
    chip.draw(x=x, y=y, height=n)
  }
}