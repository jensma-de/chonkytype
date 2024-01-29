package com.jensma.chonkytype

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import java.util.Locale


class KeyboardService : InputMethodService() {
    private lateinit var vibrator: Vibrator
    private lateinit var inputView: View
    private var isShiftPressed = false
    private var isSwapEnabled = false
    private var shouldVibrate = true
    private var shouldPhysiVibrate = true
    private var isAltKeyboardEnabled = false

    private var isAltPressed = false
    private var isEmojiMenuOpen = false
    private var isKeyBeingPressed = false
    //private var isStickyAltActive = false
    private var isstickyaltenabled = false

    private var lastAltPressTime: Long = 0
    private val doubleClickInterval: Long = 300 // time for double click sticky alt menu
    private var altTogglePermanent = false
    private var altKeyPressed = false

    /*
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateKeyboardLayout()
        }
    }
   */

    private fun toggleEmojiMenu() {
        val emojiScrollView = inputView.findViewById<ScrollView>(R.id.emoji_scroll_view)
        if (emojiScrollView.visibility == View.GONE) {
            emojiScrollView.visibility = View.VISIBLE
            isEmojiMenuOpen = true
        } else {
            emojiScrollView.visibility = View.GONE
            isEmojiMenuOpen = false
        }
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        if (isEmojiMenuOpen) {
            toggleEmojiMenu()
        }


        altTogglePermanent = false
        toggleAltKeyboard(false)
        isAltPressed = false
        altKeyPressed = false

        inputView.requestFocus()
    }

    private fun showOnScreenKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private val settingsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.chonkytype.chonkytype.ACTION_UPDATE_KEYBOARD") {
                loadSettings()
                updateKeyboardLayout()
                updateKeyLabels()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        registerReceiver(settingsUpdateReceiver, IntentFilter("com.chonkytype.chonkytype.ACTION_UPDATE_KEYBOARD"))
        loadSettings()
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        isSwapEnabled = sharedPreferences.getBoolean("swap_keys", false)
        shouldVibrate = sharedPreferences.getBoolean("vibrate", true)
        shouldPhysiVibrate = sharedPreferences.getBoolean("physivibrate", true)
        isAltKeyboardEnabled = sharedPreferences.getBoolean("altkeyboard", false)
        isstickyaltenabled = sharedPreferences.getBoolean("stickyalt", false)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(settingsUpdateReceiver)
    }

/* always enabled
    private fun isAutoPopupEnabled(): Boolean {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("autopopup_enabled", false)
    }
*/

    private fun updateKeyboardLayout() {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        val buttonIdsToKeys = mapOf(
            R.id.b1 to "button_label_1",
            R.id.b2 to "button_label_2",
            R.id.b3 to "button_label_3",
            R.id.b4 to "button_label_4",
            R.id.b5 to "button_label_5",
            R.id.b6 to "button_label_6",
            R.id.b7 to "button_label_7",
            R.id.b8 to "button_label_8",
            R.id.b9 to "button_label_9",
            R.id.b10 to "button_label_10"


        )

        buttonIdsToKeys.forEach { (buttonId, key) ->
            val button = inputView.findViewById<Button>(buttonId)
            val label = sharedPreferences.getString(key, "?") ?: "?"
            button.text = label
            setupButton(button, key, sharedPreferences)
        }
    }

    private fun updateKeyLabels() {
        val buttonIds = listOf(R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9, R.id.b10)
        buttonIds.forEach { buttonId ->
            val button = inputView.findViewById<Button>(buttonId)
            val label = button.text.toString()
            button.text = transformLabel(label, isShiftPressed)
        }
    }

    private fun transformLabel(label: String, isShiftPressed: Boolean): String {
        return when {
            label == "ß" && isShiftPressed -> "ẞ"
            isShiftPressed -> label.toUpperCase(Locale.getDefault())
            else -> label.toLowerCase(Locale.getDefault())
        }
    }

    private fun toggleAltKeyboard(showAlt: Boolean) {

        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)

        val standardKeys = listOf("button_label_1", "button_label_2", "button_label_3", "button_label_4", "button_label_5", "button_label_6", "button_label_7", "button_label_8", "button_label_9", "button_label_10")
        val altKeys = listOf("alt_button_label_1", "alt_button_label_2", "alt_button_label_3", "alt_button_label_4", "alt_button_label_5", "alt_button_label_6", "alt_button_label_7", "alt_button_label_8", "alt_button_label_9", "alt_button_label_10")

        val buttonIds = listOf(R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9, R.id.b10)

        for (i in buttonIds.indices) {
            val button = inputView.findViewById<Button>(buttonIds[i])
            val key = if (showAlt) altKeys[i] else standardKeys[i]
            val label = sharedPreferences.getString(key, "") ?: ""
            button.text = label
            setupButton(button, key, sharedPreferences)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {

        isKeyBeingPressed = false

        if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            altKeyPressed = false
            if (!altTogglePermanent) {
                // Alt-Tastatur ausblenden, wenn sie temporär eingeblendet wurde
                toggleAltKeyboard(false)
            }
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            if (!altTogglePermanent) {
                toggleAltKeyboard(false) // Alt-Tastatur ausblenden
            }
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            isShiftPressed = false
            updateKeyLabels()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.deviceId != KeyCharacterMap.VIRTUAL_KEYBOARD) {
            showOnScreenKeyboard()
        }


        if (!isKeyBeingPressed && event.deviceId != KeyCharacterMap.VIRTUAL_KEYBOARD) {
            if (shouldVibrateOnPhysicalInput() && keyCode != KeyEvent.KEYCODE_ALT_LEFT && keyCode != KeyEvent.KEYCODE_ALT_RIGHT && keyCode != KeyEvent.KEYCODE_SHIFT_LEFT && keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT) {
                vibrate()
                isKeyBeingPressed = true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            if (!altKeyPressed) {
                altKeyPressed = true
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastAltPressTime < doubleClickInterval) {
                    // Doppelklick erkannt, Alt-Tastatur dauerhaft umschalten
                    altTogglePermanent = !altTogglePermanent
                    toggleAltKeyboard(altTogglePermanent)
                } else {
                    // Kein Doppelklick, Alt-Tastatur temporär einblenden
                    if (!altTogglePermanent) {
                        toggleAltKeyboard(true)
                    }
                }
                lastAltPressTime = currentTime
            }
            return true
        }



        isShiftPressed = event.isShiftPressed

        if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            isAltPressed = true
            if (isAltKeyboardEnabled) {
                toggleAltKeyboard(true)
            }
            return true
        }

        if (event.deviceId != KeyCharacterMap.VIRTUAL_KEYBOARD) {
            if (isSwapEnabled) {
                when (keyCode) {
                    KeyEvent.KEYCODE_Z -> {
                        val textToInput = if (isAltPressed) "!" else "y"
                        inputText(textToInput, isShiftPressed, fromPhysicalKeyboard = true)

                        return true
                    }
                    KeyEvent.KEYCODE_Y -> {
                        val textToInput = if (isAltPressed) ")" else "z"
                        inputText(textToInput, isShiftPressed, fromPhysicalKeyboard = true)

                        return true
                    }
                }
            }


        }

        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            isShiftPressed = true
            updateKeyLabels()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }




    override fun onCreateInputView(): View {
        inputView = layoutInflater.inflate(R.layout.keyboard_layout, null)
        inputView.visibility = View.GONE // Standardmäßig ist die Tastatur unsichtbar


        inputView = layoutInflater.inflate(R.layout.keyboard_layout, null)

        val emojiContainer = inputView.findViewById<LinearLayout>(R.id.emoji_container)
        updateKeyboardLayout()
        updateKeyLabels()

        /* TODO */
            val emojis = arrayOf(
                "-","-","-","-",
                "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "🫠", "😉",
                "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😊", "😚", "😙", "🥲", "😋",
                "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🫢", "🫣", "🤫", "🤔", "🫡", "🤐",
                "🤨", "😐", "😑", "😶", "🫥", "😶‍🌫️", "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "🫨",
                "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶",
                "🥴", "😵", "😵‍💫", "🤯", "🤠", "🥳", "🥸", "😎", "🤓", "🧐", "😕", "🫤", "😟",
                "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧", "😨", "😰", "😥",
                "😢", "😭", "😱", "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
                "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡", "-",
                //--------------------------------------------------------------
                "😸", "😺","😹", "😻", "😼", "😽", "🙀", "😿", "😾", "🙈", "🙉", "🙊",
                "💩","💀", "☠️", "👹", "👺", "👻", "👽", "👾", "🤖", "-",
                //--------------------------------------------------------------
                "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴", "🫷", "🫸",
                "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆",
                "🖕", "👇", "☝️", "🫵", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "🫶",
                "👐", "🤲", "🤝", "🙏", "✍️", "💪", "🦶", "👂",
                "👃", "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅","-",
                //--------------------------------------------------------------
                "💖", "💗", "💓", "💔", "❤️‍🔥", "❤️‍🩹", "❤️", "🩷",
                "🧡", "💛", "💚", "💙", "🩵", "💜", "🤎", "🖤", "🩶",  "🤍",
                "💋", "💯", "💢", "💥", "💦", "💨", "💬",
                "💤","-",
                //--------------------------------------------------------------

                "🐵", "🐶", "🐕", "🐺", "🦊", "🦝", "🐱",
                "🦁", "🐴", "🦄", "🦓",
                "🐃", "🐄", "🐷", "🐗", "🐏", "🐑", "🐐", "🐪", "🦙", "🦒", "🐘",
                "🦣", "🦏", "🐭", "🐹", "🐰", "🐿️", "🦫", "🦔", "🦇", "🐻",
                "🐻‍❄️", "🐨", "🐼","🦨", "🦘", "🦡", "🐾", "🦃", "🐔", "🐣",
                "🕊️", "🦅", "🦆", "🦢", "🦉", "🦩", "🦜",
                "🐸", "🐊", "🐢", "🐍", "🐲", "🦕", "🐳", "🐬", "🐟",
                "🦈", "🐙", "🐌", "🦋", "🐛", "🐜", "🐝", "🪲", "🐞",
                "🦗", "🪳", "🕷️", "🕸️", "🌸", "🌹",
                "🥀", "🌼", "🌷", "🌲", "🌴", "🌵", "☘️",
                "🍀","-",
                //--------------------------------------------------------------
                "🍄", "🍇", "🍉", "🍊", "🍋", "🍌", "🍍", "🥭", "🍎", "🍐", "🍑", "🍒",
                "🍓", "🫐", "🥝", "🍅", "🫒", "🥥", "🥑", "🍆", "🥔", "🥕", "🌽", "🌶️", "🫑",
                "🥦", "🧄", "🧅", "🫘","🫛", "🍞", "🥐", "🫓", "🥨",
                "🥞", "🧇", "🧀", "🍖", "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯",
                "🫔", "🥚", "🫕", "🥣", "🧂",
                "🍙", "🍜", "🍝", "🍣", "🍤",
                "🦞", "🦐", "🍦", "🍩",
                "🍪", "🍰", "🧁", "🍫", "🍭", "🥛", "☕", "🫖",
                "🍾", "🍷", "🍸", "🍹", "🍺","🥂", "🥃",
                "🧊","🍽️", "🍴", "🥄", "🔪","-",
                //--------------------------------------------------------------
                "🌍", "🗺️", "🗻", "🏕️", "🏖️", "🏜️",
                "🏝️",
                "🏤", "🏩", "🏪", "🏫", "🏰", "💒",
                "⛪", "🕌", "🛕", "🕍", "🕋", "⛺", "🌃", "🏙️",
                "♨️", "🎠", "🛝", "🎡", "🎢", "🎪", "🚅", "🚆",
                "🚐", "🚑", "🚒", "🚓",
                "🚔", "🚕", "🚗","🦽",
                "🚲", "⛽", "🛞", "🚨", "🚦",
                "⚓", "🚢", "✈️",
                "🛰️", "🚀", "🛸",
                "🌙", "🌚",
                "🌡️", "🪐", "⭐", "🌌", "☁️", "⛅", "⛈️", "🌤️", "🌥️",
                "🌦️", "🌧️", "🌨️", "🌩️", "🌪️", "🌫️", "🌬️", "🌀", "🌈", "☂️", "⛱️", "⚡",
                "❄️", "⛄", "🔥", "💧", "🌊","-",
                //--------------------------------------------------------------
                "🎃", "🎄", "🎈", "🎉", "🎊",
                "🎀", "🎁", "🎗️", "🎟️", "🎫", "🎖️", "🏆","⚽",
                "⚾", "🏀", "🏐", "🏈", "🎳",
                "🏓", "🥊", "🥋", "🥅", "⛳", "🎣",
                "🎯", "🪀", "🪁", "🎱", "🔮", "🪄", "🎮", "🎲",
                "🧸", "🪅", "🪩", "🪆", "♠️", "♥️", "♦️", "♣️", "♟️",
                "🎨", "🕶️", "🦺", "👔",
                "🧦", "👗", "🩱",
                "👛",
                "🪮", "👑", "👒", "🎩", "🎓", "🪖", "💄", "💍",
                "💎", "🎵",
                "🎤", "🎧",  "🎹", "🎺", "🎻",
                "📱", "📞",
                "💾", "💿",
                "📸", "📼", "🔎", "💡",
                "📃",
                "💰", "🪙", "💳",
                "💹", "✉️", "📤", "📥", "📦", "📭",
                "📮", "✏️", "🖊️", "📝", "📁",
                "📅", "🗒️", "📇", "📈", "📍", "📎", "📏",
                "✂️", "🗑️", "🔒", "🔓", "🗝️", "🔨", "🪓",
                "⛏️", "⚔️", "🪃", "🏹", "🪚", "🔧", "🪛","⚙️",
                "🧲", "-",
                //--------------------------------------------------------------
                "🏧", "🚮", "🚰", "♿", "🚹", "🚺", "🚻", "🚾",
                "⚠️", "🚸", "⛔", "🚫", "🚳", "🚭", "🚯", "🚱", "🚷", "📵", "🔞", "☢️", "☣️",
                "🔆", "📶", "🛜", "♀️", "♂️", "⚧️", "✖️", "➕",
                "➖", "➗", "🟰", "‼️", "⁉️", "❓", "❗", "〰️", "💲", "⚕️",
                "♻️", "⭕", "✅", "☑️", "✔️", "❌", "❎", "〽️",  "©️", "®️", "™️",
                "-",


                //--------------------------------------------------------------
                "🇦🇷", "🇦🇺", "🇧🇪", "🇧🇷", "🇨🇦", "🇨🇳", "🇩🇰", "🇪🇬", "🇫🇷", "🇩🇪", "🇬🇷", "🇮🇳", "🇮🇩",
                "🇮🇷", "🇮🇪", "🇮🇱", "🇮🇹", "🇯🇵", "🇰🇷", "🇲🇽", "🇳🇱", "🇳🇬", "🇳🇴", "🇵🇰", "🇵🇱", "🇵🇹",
                "🇷🇺", "🇸🇦", "🇸🇬", "🇿🇦", "🇪🇸", "🇸🇪", "🇨🇭", "🇹🇼", "🇹🇭", "🇹🇷", "🇦🇪", "🇬🇧", "🇺🇸", "🇻🇳"
            )

        var rowLayout: LinearLayout? = null
        var emojiCount = 0

        emojis.forEach { emoji ->
            if (emoji == "-") {

                val separator = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        10 // Höhe der Linie
                    ).also {
                        it.setMargins(0, 10, 0, 10)
                    }
                    setBackgroundColor(Color.LTGRAY)
                }
                emojiContainer.addView(separator)

                rowLayout = null
                emojiCount = 0
            } else {

                if (rowLayout == null || emojiCount == 10) {
                    rowLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    emojiContainer.addView(rowLayout)
                    emojiCount = 0
                }

                val emojiButton = Button(this).apply {
                    text = emoji
                    textSize = 32f
                    layoutParams = LinearLayout.LayoutParams(
                        145,
                        140
                    )
                    setBackgroundColor(resources.getColor(android.R.color.transparent))
                    setPadding(0, 0, 0, 0)

                    setOnClickListener { inputEmoji(emoji) }
                }
                rowLayout?.addView(emojiButton)
                emojiCount++
            }
        }
       // val inflater = layoutInflater
        //val inputView = inflater.inflate(R.layout.keyboard_layout, null)
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)

        setupButton(inputView.findViewById(R.id.b1), "button_label_1", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b2), "button_label_2", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b3), "button_label_3", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b4), "button_label_4", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b5), "button_label_5", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b6), "button_label_6", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b7), "button_label_7", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b8), "button_label_8", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b9), "button_label_9", sharedPreferences)
        setupButton(inputView.findViewById(R.id.b10), "button_label_10", sharedPreferences)

        return inputView
    }




    private fun setupButton(button: Button, key: String, sharedPreferences: SharedPreferences) {
        val label = sharedPreferences.getString(key, "") ?: ""
        button.text = label
        button.setOnClickListener {
            if (label == "\uD83D\uDE00") {
                toggleEmojiMenu()
            } else {
                inputText(label, isShiftPressed, fromPhysicalKeyboard = false)
            }
        }
    }




    private fun inputEmoji(emoji: String) {
        if (shouldVibrate()) {
            vibrate()
        }
        currentInputConnection?.commitText(emoji, 1)
        toggleEmojiMenu()

    }

    private fun inputText(text: String, isShiftPressed: Boolean, fromPhysicalKeyboard: Boolean = false) {
        val outputText = when {
            text == "ß" && isShiftPressed -> "ẞ"
            isShiftPressed -> text.toUpperCase(Locale.getDefault())
            else -> text.toLowerCase(Locale.getDefault())
        }

        currentInputConnection?.commitText(outputText, 1)

        if (!fromPhysicalKeyboard && shouldVibrate()) {
            vibrate()
        }
    }
    fun shouldVibrate(): Boolean {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("vibrate", true)
    }

    fun shouldVibrateOnPhysicalInput(): Boolean {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("physivibrate", true)
    }

    fun stickyaltenabled(): Boolean {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("stickyalt", true)
    }

    private fun vibrate() {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        val vibrationLength = sharedPreferences.getInt("vibration_length", 5)

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(vibrationLength.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationLength.toLong())
        }
    }
}



