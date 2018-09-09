package chipset.instructions

import Chip8

// #XNN map
private val XNNMap = hashMapOf(
    0x3000 to ::skipNextInstructionIfCellsAreEqual,	// "3XNN" Cond	if(Vx==NN)	Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
    0x4000 to ::skipNextInstructionIfCellsNotAreEqual,	// "4XNN" Cond	if(Vx!=NN)	Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
    0x6000 to ,	// "6XNN" Const	Vx = NN	Sets VX to NN.
    0x7000 to XNN(),	// "7XNN" Const	Vx += NN	Adds NN to VX. (Carry flag is not changed)
    0xC000 to XNN()	  // "CXNN" Rand	Vx=rand()&NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
)

private fun skipNextInstructionIfCellsAreEqual(chip: Chip8, cell1: Int, cell2: Int) {
  chip.skipNextInstructionIfCellsAreEqual(cell1, cell2)
}

private fun skipNextInstructionIfCellsNotAreEqual(chip: Chip8, cell1: Int, cell2: Int) {
  chip.skipNextInstructionIfCellsNotAreEqual(cell1, cell2)
}

class XNN : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val x = instruction and 0x0F00
    val biggerVal = instruction and 0x00FF
    val key = instruction and 0xF000
    XNNMap[key]?.invoke(chip, x, biggerVal)
  }
}