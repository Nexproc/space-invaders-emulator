package chipset.instructions

import Chip8

// #XY# map
private val XYMap = hashMapOf(
    0x5000 to ::skipNextInstructionIfEqual, // "5XY0" Cond	if(Vx==Vy)	Skips the next instruction if VX equals VY. (Usually the next instruction is a jump to skip a code block)
    0x8000 to ::setRegisterXToRegisterY, // "8XY0" Assign	Vx=Vy	Sets VX to the value of VY.
    0x8001 to ::setRegisterXToRegisterYORRegisterX, // "8XY1" BitOp	Vx=Vx|Vy	Sets VX to VX or VY. (Bitwise OR operation)
    0x8002 to ::setRegisterXToRegisterYANDRegisterX, // "8XY2" BitOp	Vx=Vx&Vy	Sets VX to VX and VY. (Bitwise AND operation)
    0x8003 to ::setRegisterXToRegisterYXORRegisterX, // "8XY3" BitOp	Vx=Vx^Vy	Sets VX to VX xor VY.
    0x8004 to ::setRegisterXToRegisterYPlusRegisterX, // "8XY4" Math	Vx += Vy	Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
    0x8005 to ::setRegisterXToRegisterXMinusRegisterY, // "8XY5" Math	Vx -= Vy	VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    0x8006 to ::shiftXRightMoveXLeastSignificantBitToCarry, // "8XY6" BitOp	Vx>>=1	Stores the least significant bit of VX in VF and then shifts VX to the right by 1.[2]
    0x8007 to ::setRegisterXToRegisterYMinusRegisterX, // "8XY7" Math	Vx=Vy-Vx	Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    0x800E to ::shiftXLeftMoveXSignificantBitToCarry, // "8XYE" BitOp	Vx<<=1	Stores the most significant bit of VX in VF and then shifts VX to the left by 1.[3]
    0x9000 to ::skipInstructionIfRegisterXDoesNotEqualRegisterY  // "9XY0" cond	if(Vx!=Vy)	Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
)

private fun skipNextInstructionIfEqual(chip: Chip8, x: Int, y: Int) {
  chip.skipNextInstructionIfValuesAreEqual(x, y)
}

private fun setRegisterXToRegisterY(chip: Chip8, x: Int, y: Int) {
  chip.setRegisterToValue(x, chip.getRegisterValue(y))
}

private fun setRegisterXToRegisterYORRegisterX(chip: Chip8, x: Int, y: Int) {
  chip.setRegisterToValue(
      register = x,
      value = chip.getRegisterValue(x) or chip.getRegisterValue(y))
}

private fun setRegisterXToRegisterYANDRegisterX(chip: Chip8, x: Int, y: Int) {
  chip.setRegisterToValue(
      register = x,
      value = chip.getRegisterValue(x) and chip.getRegisterValue(y))
}

private fun setRegisterXToRegisterYXORRegisterX(chip: Chip8, x: Int, y: Int) {
  chip.setRegisterToValue(
      register = x,
      value = chip.getRegisterValue(x) xor chip.getRegisterValue(y))
}

private fun setRegisterXToRegisterYPlusRegisterX(chip: Chip8, x: Int, y: Int) {
  var value = chip.getRegisterValue(x) + chip.getRegisterValue(y)
  if (value > 255) {
    chip.flipCarryFlag()
    value = 255
  } else { chip.unflipCarryFlag() }
  chip.setRegisterToValue(register = x, value = value)
}

private fun setRegisterXToRegisterXMinusRegisterY(chip: Chip8, x: Int, y: Int) {
  var value = chip.getRegisterValue(x) - chip.getRegisterValue(y)
  if (value < 0) {
    chip.unflipCarryFlag()
    value = 0
  } else { chip.flipCarryFlag() }
  chip.setRegisterToValue(register = x, value = value)
}
// "8XY6" BitOp	Vx>>=1	Stores the least significant bit of VX in VF and then shifts VX to the right by 1.[2]
private fun shiftXRightMoveXLeastSignificantBitToCarry(chip: Chip8, x: Int, y: Int) {
  val leastSig = chip.getRegisterValue(x) and 0x0001
  val newX = chip.getRegisterValue(x) shr 1
  chip.setRegisterToValue(register = 15, value = leastSig)
  chip.setRegisterToValue(register = x, value = newX)
}

// "8XYE" BitOp	Vx<<=1	Stores the most significant bit of VX in VF and then shifts VX to the left by 1.[3]
fun shiftXLeftMoveXSignificantBitToCarry(chip: Chip8, x: Int, y: Int) {
  val xShifted = chip.getRegisterValue(x) shl 1
  val newX = xShifted and 0x000F
  val mostSig = xShifted and 0x00F0
  chip.setRegisterToValue(register = 15, value = mostSig)
  chip.setRegisterToValue(register = x, value = newX)
}

// "8XY7" Math	Vx=Vy-Vx	Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
private fun setRegisterXToRegisterYMinusRegisterX(chip: Chip8, x: Int, y: Int) {
  var value = chip.getRegisterValue(y) - chip.getRegisterValue(x)
  if (value < 0) {
    chip.unflipCarryFlag()
    value = 0 // is this 0 or value * -1 ?
  } else { chip.flipCarryFlag() }
  chip.setRegisterToValue(register = x, value = value)
}

private fun skipInstructionIfRegisterXDoesNotEqualRegisterY(chip: Chip8, x: Int, y: Int) {
  val valx = chip.getRegisterValue(x)
  val valy = chip.getRegisterValue(y)
  chip.skipNextInstructionIfValuesAreEqual(valx, valy)
}

class XY : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val x = (instruction and 0x0F00) shr 8
    val y = (instruction and 0x00F0) shr 4
    val key = (instruction and 0xF00F)
    XYMap[key]?.invoke(chip, x, y)
  }
}