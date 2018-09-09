package chipset

import Chip8
import chipset.instructions.*

// #NNN map
private val NNNMap = hashMapOf(
    0x0000 to NNN(),	// "0NNN" Call		Calls RCA 1802 program at address nnn. Not necessary for most ROMs.
    0x1000 to NNN(),	// "1NNN" Flow	goto nnn;	Jumps to address nnn.
    0x2000 to NNN(),	// "2NNN" Flow	*(0xNNN)()	Calls subroutine at nnn.
    0xA000 to NNN(),	// "ANNN" MEM	I = nnn	Sets I to the address nnn.
    0xB000 to NNN()	  // "BNNN" Flow	PC=V0+nnn	Jumps to the address nnn plus V0.
)
// #XNN map
private val XNNMap = hashMapOf(
    0x3000 to XNN(),	// "3XNN" Cond	if(Vx==NN)	Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
    0x4000 to XNN(),	// "4XNN" Cond	if(Vx!=NN)	Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
    0x6000 to XNN(),	// "6XNN" Const	Vx = NN	Sets VX to NN.
    0x7000 to XNN(),	// "7XNN" Const	Vx += NN	Adds NN to VX. (Carry flag is not changed)
    0xC000 to XNN()	  // "CXNN" Rand	Vx=rand()&NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
)
// #XY# map
private val XYMap = hashMapOf(
    0x5000 to XY(),	// "5XY0" Cond	if(Vx==Vy)	Skips the next instruction if VX equals VY. (Usually the next instruction is a jump to skip a code block)
    0x8000 to XY(),	// "8XY0" Assign	Vx=Vy	Sets VX to the value of VY.
    0x8001 to XY(),	// "8XY1" BitOp	Vx=Vx|Vy	Sets VX to VX or VY. (Bitwise OR operation)
    0x8002 to XY(),	// "8XY2" BitOp	Vx=Vx&Vy	Sets VX to VX and VY. (Bitwise AND operation)
    0x8003 to XY(),	// "8XY3" BitOp	Vx=Vx^Vy	Sets VX to VX xor VY.
    0x8004 to XY(),	// "8XY4" Math	Vx += Vy	Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
    0x8005 to XY(),	// "8XY5" Math	Vx -= Vy	VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    0x8006 to XY(),	// "8XY6" BitOp	Vx>>=1	Stores the least significant bit of VX in VF and then shifts VX to the right by 1.[2]
    0x8007 to XY(),	// "8XY7" Math	Vx=Vy-Vx	Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    0x800E to XY(),	// "8XYE" BitOp	Vx<<=1	Stores the most significant bit of VX in VF and then shifts VX to the left by 1.[3]
    0x9000 to XY()	  // "9XY0" cond	if(Vx!=Vy)	Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
)
// XYN Map
private val XYNMap = hashMapOf(
    0xD000 to XYN()	  // "DXYN" Disp	draw(Vx,Vy,N)	Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. Each row of 8 pixels is read as bit-coded starting from memory location I; I value doesn’t change after the execution of this instruction. As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen
)

// #X## map
private val XMap = hashMapOf(
    0xE09E to X(),	// "EX9E" KeyOp	if(key()==Vx)	Skips the next instruction if the key stored in VX is pressed. (Usually the next instruction is a jump to skip a code block)
    0xE0A1 to X(),	// "EXA1" KeyOp	if(key()!=Vx)	Skips the next instruction if the key stored in VX isn't pressed. (Usually the next instruction is a jump to skip a code block)
    0xF007 to X(),	// "FX07" Timer	Vx = get_delay()	Sets VX to the value of the delay timer.
    0xF00A to X(),	// "FX0A" KeyOp	Vx = get_key()	A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
    0xF015 to X(),	// "FX15" Timer	delay_timer(Vx)	Sets the delay timer to VX.
    0xF018 to X(),	// "FX18" Sound	sound_timer(Vx)	Sets the sound timer to VX.
    0xF01E to X(),	// "FX1E" MEM	I +=Vx	Adds VX to I.[4]
    0xF029 to X(),	// "FX29" MEM	I=sprite_addr[Vx]	Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
    0xF033 to X(),	// "FX33" BCD	set_BCD(Vx);
    // *(I+0)=BCD(3);
    // *(I+1)=BCD(2);
    // *(I+2)=BCD(1);
    // Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
    0xF055 to X(),	// "FX55" MEM	reg_dump(Vx,&I)	Stores V0 to VX (including VX) in memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
    0xF065 to X()	  // "FX65" MEM	reg_load(Vx,&I)	Fills V0 to VX (including VX) with values from memory starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
)
// unique map e.g. "00E0", "00EE"
private val uniqueMap = hashMapOf(
    0x00E0 to UniqueInstruction(),	// Display	disp_clear()	Clears the screen.
    0x00EE to UniqueInstruction()	  // Flow	return;	Returns from a subroutine.
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

  fun lookForCode() {

  }

  fun parseNNN(code: Int) {
    // mask 0x0FFF
    return NNNMap.getOrDefault(0x0FFF and code, defaultValue = null)
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