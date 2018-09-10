package chipset.instructions

import Chip8
import java.util.*

// #XNN map
private val XNNMap = hashMapOf(
    0x3000 to ::skipNextInstructionIfCellsAreEqual,	// "3XNN" Cond	if(Vx==NN)	Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
    0x4000 to ::skipNextInstructionIfCellsNotAreEqual,	// "4XNN" Cond	if(Vx!=NN)	Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
    0x6000 to ::setRegisterToValue,	// "6XNN" Const	Vx = NN	Sets VX to NN.
    0x7000 to ::addValueToRegister,	// "7XNN" Const	Vx += NN	Adds NN to VX. (Carry flag is not changed)
    0xC000 to ::setRegisterToRandomValue // "CXNN" Rand	Vx=rand()&NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
)

private fun skipNextInstructionIfCellsAreEqual(chip: Chip8, x: Int, nn: Int) {
  chip.skipNextInstructionIfCellsAreEqual(x, nn)
}

private fun skipNextInstructionIfCellsNotAreEqual(chip: Chip8, x: Int, nn: Int) {
  chip.skipNextInstructionIfCellsNotAreEqual(x, nn)
}

private fun setRegisterToValue(chip: Chip8, x: Int, nn: Int) {
  chip.setRegisterToValue(register=x, value=nn)
}

private fun addValueToRegister(chip: Chip8, x: Int, nn: Int) {
  chip.addValueToRegister(register=x, value=nn)
}

/*
 * uses nn as the "random maximum" where the random operation outputs a number between 0-255.
 * e.g. even if we rand to the max 0x00FF (255), if NN is set to 0x000F (15) we can only get values <= 15.
 */
private fun setRegisterToRandomValue(chip: Chip8, x: Int, nn: Int) {
  val rand = (0..256).random() and nn
  chip.setRegisterToValue(register=x, value=rand)
}

fun ClosedRange<Int>.random() =
    Random().nextInt((endInclusive + 1) - start) +  start

class XNN : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val x = (instruction and 0x0F00) shr 8
    val nn = instruction and 0x00FF
    val key = instruction and 0xF000
    XNNMap[key]?.invoke(chip, x, nn)
  }
}