package chipset.instructions

import Chip8

// #X## map
private val XMap = hashMapOf(
    0xE09E to ::ifKeyMatchSkipNextInstruction,	// "EX9E" KeyOp	if(key()==Vx)	Skips the next instruction if the key stored in VX is pressed. (Usually the next instruction is a jump to skip a code block)
    0xE0A1 to ::ifKeyDoesNotMatchSkipNextInstruction,	// "EXA1" KeyOp	if(key()!=Vx)	Skips the next instruction if the key stored in VX isn't pressed. (Usually the next instruction is a jump to skip a code block)
    0xF007 to ::setRegisterToDelayValue,	// "FX07" Timer	Vx = get_delay()	Sets VX to the value of the delay timer.
    0xF00A to ::awaitKeyPressThenStoreInRegister,	// "FX0A" KeyOp	Vx = get_key()	A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
    0xF015 to ::setDelayTimerFromRegistry,	// "FX15" Timer	delay_timer(Vx)	Sets the delay timer to VX.
    0xF018 to ::setSoundTimerFromRegistry,	// "FX18" Sound	sound_timer(Vx)	Sets the sound timer to VX.
    0xF01E to ::increaseRegisterIndexByX,	// "FX1E" MEM	I +=Vx	Adds VX to I.[4]
    0xF029 to X(),	// "FX29" MEM	I=sprite_addr[Vx]	Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
    0xF033 to X(),	// "FX33" BCD	set_BCD(Vx);
    // *(I+0)=BCD(3);
    // *(I+1)=BCD(2);
    // *(I+2)=BCD(1);
    // Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
    0xF055 to X(),	// "FX55" MEM	reg_dump(Vx,&I)	Stores V0 to VX (including VX) in memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
    0xF065 to X()	  // "FX65" MEM	reg_load(Vx,&I)	Fills V0 to VX (including VX) with values from memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
)

private fun ifKeyMatchSkipNextInstruction(chip: Chip8, x: Int) {
  chip.skipNextInstructionIfValuesAreEqual(key, x)
}

private fun ifKeyDoesNotMatchSkipNextInstruction(chip: Chip8, x: Int) {
  chip.skipNextInstructionIfValuesAreNotEqual(key, x)
}

private fun setRegisterToDelayValue(chip: Chip8, x: Int) {
  chip.setRegisterToValue(register = x, value = chip.getDelay())
}

// TODO: awaitKeyPress
private fun awaitKeyPressThenStoreInRegister(chip: Chip8, x: Int) {
  val key = chip.awaitKeyPress()
  chip.setRegisterToValue(register = x, value = key)
}

// TODO: setDelayTimer
private fun setDelayTimerFromRegistry(chip: Chip8, x: Int) {
  chip.setDelayTimer(chip.getRegisterValue(x))
}

// TODO: setSoundTimer
private fun setSoundTimerFromRegistry(chip: Chip8, x: Int) {
  chip.setSoundTimer(chip.getRegisterValue(x))
}

private fun increaseRegisterIndexByX(chip: Chip8, x: Int) {
  chip.setRegisterIndex(chip.getRegisterIndex() + x)
}

class X : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val x = (instruction and 0x0F00 shr 8)
    val key = (instruction and 0xF0FF)
    XMap[key]?.invoke(chip, x)
  }
}