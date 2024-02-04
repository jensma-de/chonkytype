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
import android.util.Log
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
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

    private var isstickyaltenabled = false

    private var lastAltPressTime: Long = 0
    private val doubleClickInterval: Long = 300 // time for double click sticky alt menu
    private var altTogglePermanent = false
    private var altKeyPressed = false
    private val categoryLinePositions = mutableMapOf<Int, Int>()

    private val emoji_I = arrayOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
         "🫠", "😉",  "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😊",
        "😚", "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🫢", "🫣", "🤫", "🤔", "🫡", "🤐",
        "🤨", "😐", "😑", "😶", "🫥", "😶‍🌫️", "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "🫨",
        "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶",
        "🥴", "😵", "😵‍💫", "🤯", "🤠", "🥳", "🥸", "😎", "🤓", "🧐", "😕", "🫤", "😟",
        "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧", "😨", "😰", "😥",
        "😢", "😭", "😱", "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
        "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡")

    private val emoji_II = arrayOf(
        "😸", "😺","😹", "😻", "😼", "😽", "🙀", "😿", "😾", "🙈", "🙉", "🙊",
        "💩","💀", "☠️", "👹", "👺", "👻", "👽", "👾", "🤖",
        "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴", "🫷", "🫸",
        "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆",
        "🖕", "👇", "☝️", "🫵", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "🫶",
        "👐", "🤲", "🤝", "🙏", "✍️", "💪", "🦶", "👂",
        "👃", "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅")

    private val emoji_III = arrayOf(
        "💖", "💗", "💓", "💔", "❤️‍🔥", "❤️‍🩹", "❤️", "🩷",
        "🧡", "💛", "💚", "💙", "🩵", "💜", "🤎", "🖤", "🩶",  "🤍",
        "💋", "💯", "💢", "💥", "💦", "💨", "💬",
        "💤")

    private val emoji_IV = arrayOf(
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
        "🍀")

    private val emoji_V = arrayOf(
        "🍄", "🍇", "🍉", "🍊", "🍋", "🍌", "🍍", "🥭", "🍎", "🍐", "🍑", "🍒",
        "🍓", "🫐", "🥝", "🍅", "🫒", "🥥", "🥑", "🍆", "🥔", "🥕", "🌽", "🌶️", "🫑",
        "🥦", "🧄", "🧅", "🫘","🫛", "🍞", "🥐", "🫓", "🥨",
        "🥞", "🧇", "🧀", "🍖", "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯",
        "🫔", "🥚", "🫕", "🥣", "🧂", "🍙", "🍜", "🍝", "🍣", "🍤",
        "🦞", "🦐", "🍦", "🍩", "🍪", "🍰", "🧁", "🍫", "🍭", "🥛", "☕", "🫖",
        "🍾", "🍷", "🍸", "🍹", "🍺","🥂", "🥃",
        "🧊","🍽️", "🍴", "🥄", "🔪")

    private val emoji_VI = arrayOf(
        "🌍", "🗺️", "🗻", "🏕️", "🏖️", "🏜️",
        "🏝️", "🏤", "🏩", "🏪", "🏫", "🏰", "💒",
        "⛪", "🕌", "🛕", "🕍", "🕋", "⛺", "🌃", "🏙️",
        "♨️", "🎠", "🛝", "🎡", "🎢", "🎪", "🚅", "🚆",
        "🚐", "🚑", "🚒", "🚓","🚔", "🚕", "🚗","🦽",
        "🚲", "⛽", "🛞", "🚨", "🚦", "⚓", "🚢", "✈️",
        "🛰️", "🚀", "🛸","🌙", "🌚",
        "🌡️", "🪐", "⭐", "🌌", "☁️", "⛅", "⛈️", "🌤️", "🌥️",
        "🌦️", "🌧️", "🌨️", "🌩️", "🌪️", "🌫️", "🌬️", "🌀", "🌈", "☂️", "⛱️", "⚡",
        "❄️", "⛄", "🔥", "💧", "🌊")

    private val emoji_VII = arrayOf(
        "🎃", "🎄", "🎈", "🎉", "🎊",
        "🎀", "🎁", "🎗️", "🎟️", "🎫", "🎖️", "🏆","⚽",
        "⚾", "🏀", "🏐", "🏈", "🎳",
        "🏓", "🥊", "🥋", "🥅", "⛳", "🎣",
        "🎯", "🪀", "🪁", "🎱", "🔮", "🪄", "🎮", "🎲",
        "🧸", "🪅", "🪩", "🪆", "♠️", "♥️", "♦️", "♣️", "♟️",
        "🎨", "🕶️", "🦺", "👔",
        "🧦", "👗", "🩱", "👛",
        "🪮", "👑", "👒", "🎩", "🎓", "🪖", "💄", "💍",
        "💎", "🎵", "🎤", "🎧",  "🎹", "🎺", "🎻",
        "📱", "📞", "💾", "💿",
        "📸", "📼", "🔎", "💡",
        "📃", "💰", "🪙", "💳",
        "💹", "✉️", "📤", "📥", "📦", "📭",
        "📮", "✏️", "🖊️", "📝", "📁",
        "📅", "🗒️", "📇", "📈", "📍", "📎", "📏",
        "✂️", "🗑️", "🔒", "🔓", "🗝️", "🔨", "🪓",
        "⛏️", "⚔️", "🪃", "🏹", "🪚", "🔧", "🪛","⚙️",
        "🧲")
    val emoji_VIII = arrayOf(
        "🏧", "🚮", "🚰", "♿", "🚹", "🚺", "🚻", "🚾",
        "⚠️", "🚸", "⛔", "🚫", "🚳", "🚭", "🚯", "🚱", "🚷", "📵", "🔞", "☢️", "☣️",
        "🔆", "📶", "🛜", "♀️", "♂️", "⚧️", "✖️", "➕",
        "➖", "➗", "🟰", "‼️", "⁉️", "❓", "❗", "〰️", "💲", "⚕️",
        "♻️", "⭕", "✅", "☑️", "✔️", "❌", "❎", "〽️",  "©️", "®️", "™️")


    val emoji_IX = arrayOf(
        "🇦🇷", "🇦🇺", "🇧🇪", "🇧🇷", "🇨🇦", "🇨🇳", "🇩🇰", "🇪🇬", "🇫🇷", "🇩🇪", "🇬🇷", "🇮🇳", "🇮🇩",
        "🇮🇷", "🇮🇪", "🇮🇱", "🇮🇹", "🇯🇵", "🇰🇷", "🇲🇽", "🇳🇱", "🇳🇬", "🇳🇴", "🇵🇰", "🇵🇱", "🇵🇹",
        "🇷🇺", "🇸🇦", "🇸🇬", "🇿🇦", "🇪🇸", "🇸🇪", "🇨🇭", "🇹🇼", "🇹🇭", "🇹🇷", "🇦🇪", "🇬🇧", "🇺🇸", "🇻🇳","\uD83C\uDFF3\uFE0F","\uD83C\uDFC1","\uD83C\uDFF4","\uD83C\uDFF4\u200D☠\uFE0F","\uD83C\uDFF3\uFE0F\u200D⚧\uFE0F"
    )


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

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        registerReceiver(settingsUpdateReceiver, IntentFilter("com.chonkytype.chonkytype.ACTION_UPDATE_KEYBOARD"))
        loadSettings()
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
                toggleAltKeyboard(false)
            }
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            if (!altTogglePermanent) {
                toggleAltKeyboard(false)
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

                    altTogglePermanent = !altTogglePermanent
                    toggleAltKeyboard(altTogglePermanent)
                } else {

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





    private fun scrollToTop() {
        val scrollView = inputView.findViewById<ScrollView>(R.id.emoji_scroll_view)
        scrollView.post { scrollView.scrollTo(0, 0) }
    }


    private fun setupCategoryButtons() {
        inputView.findViewById<Button>(R.id.category_1).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToTop()
        }
        inputView.findViewById<Button>(R.id.category_2).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(0)
        }
        inputView.findViewById<Button>(R.id.category_3).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(1)
        }
        inputView.findViewById<Button>(R.id.category_4).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(2)
        }
        inputView.findViewById<Button>(R.id.category_5).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(3)
        }
        inputView.findViewById<Button>(R.id.category_6).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(4)
        }
        inputView.findViewById<Button>(R.id.category_7).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(5)
        }
        inputView.findViewById<Button>(R.id.category_8).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(6)
        }
        inputView.findViewById<Button>(R.id.category_9).setOnClickListener {
            if (shouldVibrate()) {vibrate()}
            scrollToCategoryLine(7)
        }


    }



    private fun showEmojiCategory(emojis: Array<String>) {
        val emojiContainer = inputView.findViewById<LinearLayout>(R.id.emoji_container)
        emojiContainer.removeAllViews()
        fillCategoryWithEmojis(emojiContainer, emojis)
    }
    private fun scrollToCategoryLine(categoryIndex: Int) {
        val scrollView = inputView.findViewById<ScrollView>(R.id.emoji_scroll_view)
        val emojiContainer = inputView.findViewById<LinearLayout>(R.id.emoji_container)

        categoryLinePositions[categoryIndex]?.let { position ->
            val targetView = emojiContainer.getChildAt(position)
            scrollView.post { scrollView.scrollTo(0, targetView.top) }
        }
    }
    private fun fillCategoryWithEmojis(emojiContainer: LinearLayout, emojis: Array<String>) {
        val emojisPerRow = 10
        val screenWidth = resources.displayMetrics.widthPixels
        val buttonWidth = screenWidth / emojisPerRow
        var emojisAddedInRow = 0
        var categoryIndex = 0

        emojis.forEach { emoji ->
            if (emoji == "-") {
                val dividerView = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.divider_height)
                    ).also { it.setMargins(0, resources.getDimensionPixelSize(R.dimen.divider_margin_vertical), 0, resources.getDimensionPixelSize(R.dimen.divider_margin_vertical)) }
                    setBackgroundColor(Color.BLACK)
                }
                emojiContainer.addView(dividerView)


                categoryLinePositions[categoryIndex++] = emojiContainer.childCount - 1

                emojisAddedInRow = 0
                return@forEach
            }

            if (emojisAddedInRow % emojisPerRow == 0) {

                emojiContainer.addView(LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                })
                emojisAddedInRow = 0
            }

            val emojiButton = Button(this).apply {
                text = emoji
                textSize = 32f
                layoutParams = LinearLayout.LayoutParams(
                    buttonWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(resources.getColor(android.R.color.transparent))
                setPadding(0, 0, 0, 0)
                setOnClickListener { inputEmoji(emoji) }
            }


            (emojiContainer.getChildAt(emojiContainer.childCount - 1) as LinearLayout).addView(emojiButton)
            emojisAddedInRow++
        }


    }

    private fun updateCategoryButtons() {

        val firstEmojis = listOf(
            emoji_I.first(),
            emoji_II.first(),
            emoji_III.first(),
            emoji_IV.first(),
            emoji_V.first(),
            emoji_VI.first(),
            emoji_VII.first(),
            emoji_VIII.first(),
            emoji_IX.first()
        )


        inputView.findViewById<Button>(R.id.category_1).text = firstEmojis[0]
        inputView.findViewById<Button>(R.id.category_2).text = firstEmojis[1]
        inputView.findViewById<Button>(R.id.category_3).text = firstEmojis[2]
        inputView.findViewById<Button>(R.id.category_4).text = firstEmojis[3]
        inputView.findViewById<Button>(R.id.category_5).text = firstEmojis[4]
        inputView.findViewById<Button>(R.id.category_6).text = firstEmojis[5]
        inputView.findViewById<Button>(R.id.category_7).text = firstEmojis[6]
        inputView.findViewById<Button>(R.id.category_8).text = firstEmojis[7]
        inputView.findViewById<Button>(R.id.category_9).text = firstEmojis[8]
    }





    override fun onCreateInputView(): View {
        inputView = layoutInflater.inflate(R.layout.keyboard_layout, null)

        val emojiScrollView = inputView.findViewById<ScrollView>(R.id.emoji_scroll_view)

        val paddingTop = resources.getDimensionPixelSize(R.dimen.emoji_scroll_view_padding_top)
        emojiScrollView.setPadding(emojiScrollView.paddingLeft, paddingTop, emojiScrollView.paddingRight, emojiScrollView.paddingBottom)

        setupCategoryButtons()
        updateCategoryButtons()


        val superEmojiList = emoji_I + arrayOf("-") + emoji_II + arrayOf("-") + emoji_III +
                arrayOf("-") + emoji_IV + arrayOf("-") + emoji_V + arrayOf("-") +
                emoji_VI + arrayOf("-") + emoji_VII + arrayOf("-") + emoji_VIII +
                arrayOf("-") + emoji_IX

        showEmojiCategory(superEmojiList)


        updateKeyboardLayout()
        updateKeyLabels()








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
                scrollToTop()
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



