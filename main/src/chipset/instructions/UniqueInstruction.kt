package chipset.instructions

import Chip8
import chipset.doNothing

// unique map e.g. "00E0", "00EE"
private val uniqueMap = hashMapOf(
    0x00E0 to ::displayClear,	// Display	disp_clear()	Clears the screen.
    0x00EE to ::flowReturn	  // Flow	return;	Returns from a subroutine.
)

private fun displayClear(chip: Chip8) {
  chip.displayClear()
}

private fun flowReturn(chip: Chip8) {
  chip.flowReturn()
}

class UniqueInstruction : Instruction {

  override fun doOperation(chip: Chip8, instruction: Int) {
    uniqueMap[instruction]?.invoke(chip)
  }
}