package chipset

import Chip8
// #NNN map
private val NNNMap = hashMapOf(
    "0000" to ::doNothing,	// "0NNN" Call		Calls RCA 1802 program at address NNN. Not necessary for most ROMs.
    "1000" to ::doNothing,	// "1NNN" Flow	goto NNN;	Jumps to address NNN.
    "2000" to ::doNothing,	// "2NNN" Flow	*(0xNNN)()	Calls subroutine at NNN.
    "A000" to ::doNothing,	// "ANNN" MEM	I = NNN	Sets I to the address NNN.
    "B000" to ::doNothing	  // "BNNN" Flow	PC=V0+NNN	Jumps to the address NNN plus V0.
)
// #XNN map
private val XNNMap = hashMapOf(
    "3000" to ::doNothing,	// "3XNN" Cond	if(Vx==NN)	Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
    "4000" to ::doNothing,	// "4XNN" Cond	if(Vx!=NN)	Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
    "6000" to ::doNothing,	// "6XNN" Const	Vx = NN	Sets VX to NN.
    "7000" to ::doNothing,	// "7XNN" Const	Vx += NN	Adds NN to VX. (Carry flag is not changed)
    "C000" to ::doNothing	  // "CXNN" Rand	Vx=rand()&NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
)
// #XY# map
private val XYMap = hashMapOf(
    "5000" to ::doNothing,	// "5XY0" Cond	if(Vx==Vy)	Skips the next instruction if VX equals VY. (Usually the next instruction is a jump to skip a code block)
    "8000" to ::doNothing,	// "8XY0" Assign	Vx=Vy	Sets VX to the value of VY.
    "8001" to ::doNothing,	// "8XY1" BitOp	Vx=Vx|Vy	Sets VX to VX or VY. (Bitwise OR operation)
    "8002" to ::doNothing,	// "8XY2" BitOp	Vx=Vx&Vy	Sets VX to VX and VY. (Bitwise AND operation)
    "8003" to ::doNothing,	// "8XY3" BitOp	Vx=Vx^Vy	Sets VX to VX xor VY.
    "8004" to ::doNothing,	// "8XY4" Math	Vx += Vy	Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
    "8005" to ::doNothing,	// "8XY5" Math	Vx -= Vy	VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    "8006" to ::doNothing,	// "8XY6" BitOp	Vx>>=1	Stores the least significant bit of VX in VF and then shifts VX to the right by 1.[2]
    "8007" to ::doNothing,	// "8XY7" Math	Vx=Vy-Vx	Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    "800E" to ::doNothing,	// "8XYE" BitOp	Vx<<=1	Stores the most significant bit of VX in VF and then shifts VX to the left by 1.[3]
    "9000" to ::doNothing	  // "9XY0" cond	if(Vx!=Vy)	Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
)
// XYN Map
private val XYNMap = hashMapOf(
    "D000" to ::doNothing	  // "DXYN" Disp	draw(Vx,Vy,N)	Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. Each row of 8 pixels is read as bit-coded starting from memory location I; I value doesn’t change after the execution of this instruction. As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen
)

// #X## map
private val XMap = hashMapOf(
    "E09E" to ::doNothing,	// "EX9E" KeyOp	if(key()==Vx)	Skips the next instruction if the key stored in VX is pressed. (Usually the next instruction is a jump to skip a code block)
    "E0A1" to ::doNothing,	// "EXA1" KeyOp	if(key()!=Vx)	Skips the next instruction if the key stored in VX isn't pressed. (Usually the next instruction is a jump to skip a code block)
    "F007" to ::doNothing,	// "FX07" Timer	Vx = get_delay()	Sets VX to the value of the delay timer.
    "F00A" to ::doNothing,	// "FX0A" KeyOp	Vx = get_key()	A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
    "F015" to ::doNothing,	// "FX15" Timer	delay_timer(Vx)	Sets the delay timer to VX.
    "F018" to ::doNothing,	// "FX18" Sound	sound_timer(Vx)	Sets the sound timer to VX.
    "F01E" to ::doNothing,	// "FX1E" MEM	I +=Vx	Adds VX to I.[4]
    "F029" to ::doNothing,	// "FX29" MEM	I=sprite_addr[Vx]	Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
    "F033" to ::doNothing,	// "FX33" BCD	set_BCD(Vx);
    // *(I+0)=BCD(3);
    // *(I+1)=BCD(2);
    // *(I+2)=BCD(1);
    // Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
    "F055" to ::doNothing,	// "FX55" MEM	reg_dump(Vx,&I)	Stores V0 to VX (including VX) in memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
    "F065" to ::doNothing	  // "FX65" MEM	reg_load(Vx,&I)	Fills V0 to VX (including VX) with values from memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
)
// unique map e.g. "00E0", "00EE"
private val uniqueMap = hashMapOf(
    "00E0" to ::doNothing,	// Display	disp_clear()	Clears the screen.
    "00EE" to ::doNothing	  // Flow	return;	Returns from a subroutine.
)

class Chip8OpcodeInstructionDecoder(chip: Chip8) {
  fun runCodeByInstruction(code: Int) {
    parseNNN(code)
    parseXNN(code)
    parseXY(code)
    parseXYN(code)
    parseX(code)
    parseUnique(code)
  }

  fun parseNNN(code: Int) {
    // mask 0x0FFF
  }

  fun parseXNN(code: Int) {
    // mask 0x0FFF
  }

  fun parseXY(code: Int) {
    // mask 0x0FF0
  }

  fun parseXYN(code: Int) {
    // mask 0x0FFF
  }

  fun parseX(code: Int) {
    // mask 0x0F00
  }

  fun parseUnique(code: Int) {
    // mask 0x0000
  }
}

fun doNothing() {}