import chipset.Chip8FontSet
import java.io.File
import java.lang.Integer.parseUnsignedInt
import java.lang.Integer.toHexString

class Chip8 {
  //  0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
  //  0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
  //  0x200-0xFFF - Program ROM and work RAM
  var opcode: Short = 0
  var memory = Array<String?>(4096) { null }
  var registerV = CharArray(16) // VF (registerV[16]) is the carry flag

  var indexRegister: Short = 0
  var programCounter: Int = 0

  var vfx = CharArray(64 * 32)

  var delayTimer: Char? = null
  var soundTimer: Char? = null

  var stack = ShortArray(16)
  var stackPointer: Short = 0 // where I am in the stack

  var keyState = CharArray(16)

  //  0x00E0 – Clears the screen
  //  0xDXYN – Draws a sprite on the screen
  var drawFlag = false // is this a function call?

  fun initialize() {
    // Initialize registers and memory once
    programCounter = 0
    opcode = 0
    indexRegister = 0
    stackPointer = 0

    // Clear display
    vfx = CharArray(64 * 32)
    // Clear stack
    stack = ShortArray(16)
    // Clear registers V0-VF
    registerV = CharArray(16)
    // Clear memory
    memory = CharArray(4096)

    // Load fontset
    // automagic some fucking chip8_fontset?
    Chip8FontSet.fontSet.forEachIndexed { index, it ->
      memory[index] = toHexString(it)
    }

    // Reset Timers
    delayTimer = null
    soundTimer = null
  }

  fun loadGame(gamePath: String) {
    val gameFile = File(gamePath)
    require(gameFile.exists(), lazyMessage = {"Game file does not exist"})
    File(gamePath).readBytes().forEachIndexed { index, byte ->
      memory[index + 512] = String.format("%02X", byte)
    }
  }

  fun emulateCycle() {
    // Fetch Opcode
    var opcode = fetchOpcode()
    // Decode Opcode
    decodeOpcode()
    // Execute Opcode

    // Update timers
  }

  private fun fetchOpcode(): Int {
    return memoryAddrToInt(programCounter!!) shl 8 or memoryAddrToInt(programCounter!! + 1)
  }

  private fun decodeOpcode(opcode: Int) {

  }

  private fun memoryAddrToInt(address: Int): Int {
    return parseUnsignedInt(memory[address].toString())
  }

  fun setKeys() {

  }
}