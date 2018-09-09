package chipset.instructions

import Chip8

// #NNN map
private val NNNMap = hashMapOf(
    0x0000 to ::call,	// "0NNN" Call		Calls RCA 1802 program at address nnn. Not necessary for most ROMs.
    0x1000 to ::goto,	// "1NNN" Flow	goto nnn;	Jumps to address nnn.
    0x2000 to ::call,	// "2NNN" Flow	*(0xNNN)()	Calls subroutine at nnn.
    0xA000 to ::setIndexRegisterToAddress,	// "ANNN" MEM	I = nnn	Sets I to the address nnn.
    0xB000 to ::jumpToAddressPlusV0	  // "BNNN" Flow	PC=V0+nnn	Jumps to the address nnn plus V0.
)

private fun call(chip: Chip8, address: Int) {
  chip.call(address)
}

private fun goto(chip: Chip8, address: Int) {
  chip.goto(address)
}

private fun setIndexRegisterToAddress(chip: Chip8, address: Int) {
  chip.setIndexRegisterToAddress(address)
}

private fun jumpToAddressPlusV0(chip: Chip, address: Int) {
  chip.jumpToAddressPlusV0(address)
}


class NNN : Instruction {
  override fun doOperation(chip: Chip8, instruction: Int) {
    val address = instruction and 0x0FFF
    val instructionKey = instruction and 0xF000
    NNNMap[instructionKey]?.invoke(chip, address)
  }
}