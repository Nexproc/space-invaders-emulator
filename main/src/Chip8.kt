import chipset.Chip8FontSet
import com.sun.org.apache.xpath.internal.operations.Bool
import sun.security.util.BitArray
import java.io.File
import java.lang.Integer.parseUnsignedInt
import java.lang.Integer.toHexString

class Chip8 {
  //  0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
  //  0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
  //  0x200-0xFFF - Program ROM and work RAM
  var opcode: Int = 0
  var memory = IntArray(4096)
  var registerV = IntArray(16) // VF (registerV[16]) is the carry flag

  var indexRegister: Int = 0
  var programCounter: Int = 0

  var vfx = BitArray(64 * 32)

  var delayTimer: Int? = null
  var soundTimer: Int? = null

  var stack = IntArray(16)
  var stackPointer: Int = 0 // where I am in the stack

  var keyState = IntArray(16)

  //  0x00E0 – Clears the screen
  //  0xDXYN – Draws a sprite on the screen
  var drawFlag = false // is this a function call?

  fun initialize() {
    // Initialize registers and memory once
    programCounter = 0x200
    opcode = 0
    indexRegister = 0
    stackPointer = 0

    // Clear display
    displayClear()
    // Clear stack
    stack = ShortArray(16)
    // Clear registers V0-VF
    registerV = CharArray(16)
    // Clear memory
    memory = CharArray(4096)

    // Load fontset
    // automagic some fucking chip8_fontset?
    Chip8FontSet.fontSet.forEachIndexed { index, it ->
      memory[index] = it
    }

    // Reset Timers
    delayTimer = null
    soundTimer = null
  }

  fun loadGame(gamePath: String) {
    val gameFile = File(gamePath)
    require(gameFile.exists(), lazyMessage = {"Game file does not exist"})
    File(gamePath).readBytes().forEachIndexed { index, byte ->
      memory[index + 512] = byte.toInt()
    }
  }

  fun emulateCycle() {
    // Fetch Opcode
    val opcode = fetchOpcode()
    // Decode Opcode
    decodeOpcode(opcode)
    // Execute Opcode

    // Update timers
  }

  private fun fetchOpcode(): Int {
//    return memoryAddrToInt(programCounter) shl 8 or memoryAddrToInt(programCounter + 1)
    return memory[programCounter] shl 8 or memory[programCounter + 1]
  }

  private fun decodeOpcode(opcode: Int) {

  }

//  private fun memoryAddrToInt(address: Int): Int {
//    return parseUnsignedInt(memory[address].toString())
//  }

  fun setKeys() {

  }

  fun displayClear() {
    vfx = BitArray(64 * 32)
  }

  fun flowReturn() {
    TODO("please do")
  }

  fun draw(x: Int, y: Int, height: Int) {
    // always has a width of 8 pixels
    val width = 8
    // get starting index from I
    var currPos = indexRegister
    for (i in 0..height) {
      for(c in 0..width) {
        if( vfx[(x+i)*(y+c)] && !intToBool(memory[indexRegister]) ) registerV[16] = 1
        vfx[(x+i)*(y+c)] = intToBool(memory[indexRegister])
        currPos += 1
      }
    }
  }

  fun call(address: Int) {
    // is this just goto?
    addToStack()
    programCounter = address
  }

  fun goto(address: Int) {
    addToStack()
    programCounter = address
  }

  private fun addToStack() {
    stack[stackPointer] = programCounter
    stackPointer += 1
  }

  fun setIndexRegisterToAddress(address: Int) {
    indexRegister = address
  }

  fun jumpToAddressPlusV0(address: Int) {
    addToStack()
    programCounter = address + registerV[0]
  }

  fun skipNextInstructionIfCellsAreEqual(cell1: Int, cell2: Int) {
    if (memory[cell1] == memory[cell2]) programCounter += 1
    programCounter += 1
  }

  fun skipNextInstructionIfCellsNotAreEqual(cell1: Int, cell2: Int) {
    if (memory[cell1] != memory[cell2]) programCounter += 1
    programCounter += 1
  }

  private fun intToBool(int: Int) = if(int == 0) False else True
}
