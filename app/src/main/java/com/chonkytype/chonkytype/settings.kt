package com.jensma.chonkytype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import android.widget.Button
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar

import android.widget.Toast
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private fun notifyKeyboardService() {
        val intent = Intent("com.chonkytype.chonkytype.ACTION_UPDATE_KEYBOARD")
        sendBroadcast(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        val vibrationLengthSeekBar = findViewById<SeekBar>(R.id.vibrationLengthSeekBar)



        val editText = findViewById<EditText>(R.id.editTextTextMultiLine3)


        val currentVibrationLength = sharedPreferences.getInt("vibration_length", 28) // Standardwert ist 28
        vibrationLengthSeekBar.progress = currentVibrationLength


        vibrationLengthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                with(sharedPreferences.edit()) {
                    putInt("vibration_length", progress)
                    apply()
                }
                notifyKeyboardService()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Weird, why does the build fails
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // when I omit this empty funs
            }
        })


        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // why do I have to declare this
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // it is not even used
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 40) {
                    s.clear()
                }
            }
        })


        if (supportActionBar != null) {
            supportActionBar?.hide()
        }

        val helpLayoutButton = findViewById<Button>(R.id.help_layout_button)
        helpLayoutButton.setOnClickListener {
            showTipsAndTricksDialog()
        }




        val applyLayoutButton = findViewById<Button>(R.id.apply_layout_button)
        applyLayoutButton.setOnClickListener {

            notifyKeyboardService()
            Toast.makeText(this, "Layout applied!", Toast.LENGTH_SHORT).show()
        }

/* always enabled
        val autopopupSwitch = findViewById<SwitchCompat>(R.id.autopopup)
        autopopupSwitch.isChecked = sharedPreferences.getBoolean("autopopup_enabled", false)
        autopopupSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("autopopup_enabled", isChecked)
                apply()
            }
            notifyKeyboardService()
        }

        val stickyAltSwitch = findViewById<SwitchCompat>(R.id.stickyalt)
        stickyAltSwitch.isChecked = sharedPreferences.getBoolean("stickyalt", false)
        stickyAltSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("stickyalt", isChecked)
                apply()
            }
            notifyKeyboardService()
        }
*/
        val vibrateSwitch = findViewById<SwitchCompat>(R.id.vibrate)
        vibrateSwitch.isChecked = sharedPreferences.getBoolean("vibrate", true)
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("vibrate", isChecked)
                apply()
            }
            notifyKeyboardService()
        }

        val physivibrate = findViewById<SwitchCompat>(R.id.physivibrate)
        physivibrate.isChecked = sharedPreferences.getBoolean("physivibrate", true)
        physivibrate.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("physivibrate", isChecked)
                apply()
            }
            notifyKeyboardService()
        }


        val swapKeysSwitch = findViewById<SwitchCompat>(R.id.switch_swap_keys)
        swapKeysSwitch.isChecked = sharedPreferences.getBoolean("swap_keys", false)
        swapKeysSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("swap_keys", isChecked)
                apply()
            }
            notifyKeyboardService()
        }

        val altKeyboardSwitch = findViewById<SwitchCompat>(R.id.altkeyboard)
        altKeyboardSwitch.isChecked = sharedPreferences.getBoolean("altkeyboard", false)
        altKeyboardSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("altkeyboard", isChecked)
                apply()
            }
            notifyKeyboardService()
        }

        val button1 = findViewById<Button>(R.id.button1)
        button1.text = sharedPreferences.getString("button_label_1", " ")
        button1.setOnClickListener { showCharacterPickerDialog(1) }
        val button2 = findViewById<Button>(R.id.button2)
        button2.text = sharedPreferences.getString("button_label_2", " ")
        button2.setOnClickListener { showCharacterPickerDialog(2) }
        val button3 = findViewById<Button>(R.id.button3)
        button3.text = sharedPreferences.getString("button_label_3", " ")
        button3.setOnClickListener { showCharacterPickerDialog(3) }
        val button4 = findViewById<Button>(R.id.button4)
        button4.text = sharedPreferences.getString("button_label_4", " ")
        button4.setOnClickListener { showCharacterPickerDialog(4) }
        val button5 = findViewById<Button>(R.id.button5)
        button5.text = sharedPreferences.getString("button_label_5", " ")
        button5.setOnClickListener { showCharacterPickerDialog(5) }
        val button6 = findViewById<Button>(R.id.button6)
        button6.text = sharedPreferences.getString("button_label_6", " ")
        button6.setOnClickListener { showCharacterPickerDialog(6) }
        val button7 = findViewById<Button>(R.id.button7)
        button7.text = sharedPreferences.getString("button_label_7", " ")
        button7.setOnClickListener { showCharacterPickerDialog(7) }
        val button8 = findViewById<Button>(R.id.button8)
        button8.text = sharedPreferences.getString("button_label_8", " ")
        button8.setOnClickListener { showCharacterPickerDialog(8) }
        val button9 = findViewById<Button>(R.id.button9)
        button9.text = sharedPreferences.getString("button_label_9", " ")
        button9.setOnClickListener { showCharacterPickerDialog(9) }
        val button10 = findViewById<Button>(R.id.button10)
        button10.text = sharedPreferences.getString("button_label_10", " ")
        button10.setOnClickListener { showCharacterPickerDialog(10) }


        val altButton1 = findViewById<Button>(R.id.button11)
        altButton1.text = sharedPreferences.getString("alt_button_label_1", " ")
        altButton1.setOnClickListener { showAltCharacterPickerDialog(11) }
        val altButton2 = findViewById<Button>(R.id.button12)
        altButton2.text = sharedPreferences.getString("alt_button_label_2", " ")
        altButton2.setOnClickListener { showAltCharacterPickerDialog(12) }
        val altButton3 = findViewById<Button>(R.id.button13)
        altButton3.text = sharedPreferences.getString("alt_button_label_3", " ")
        altButton3.setOnClickListener { showAltCharacterPickerDialog(13) }
        val altButton4 = findViewById<Button>(R.id.button14)
        altButton4.text = sharedPreferences.getString("alt_button_label_4", " ")
        altButton4.setOnClickListener { showAltCharacterPickerDialog(14) }
        val altButton5 = findViewById<Button>(R.id.button15)
        altButton5.text = sharedPreferences.getString("alt_button_label_5", " ")
        altButton5.setOnClickListener { showAltCharacterPickerDialog(15) }
        val altButton6 = findViewById<Button>(R.id.button16)
        altButton6.text = sharedPreferences.getString("alt_button_label_6", " ")
        altButton6.setOnClickListener { showAltCharacterPickerDialog(16) }
        val altButton7 = findViewById<Button>(R.id.button17)
        altButton7.text = sharedPreferences.getString("alt_button_label_7", " ")
        altButton7.setOnClickListener { showAltCharacterPickerDialog(17) }
        val altButton8 = findViewById<Button>(R.id.button18)
        altButton8.text = sharedPreferences.getString("alt_button_label_8", " ")
        altButton8.setOnClickListener { showAltCharacterPickerDialog(18) }
        val altButton9 = findViewById<Button>(R.id.button19)
        altButton9.text = sharedPreferences.getString("alt_button_label_9", " ")
        altButton9.setOnClickListener { showAltCharacterPickerDialog(19) }
        val altButton10 = findViewById<Button>(R.id.button20)
        altButton10.text = sharedPreferences.getString("alt_button_label_10", " ")
        altButton10.setOnClickListener { showAltCharacterPickerDialog(20) }


    }
    private fun showTipsAndTricksDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("Thanks to:\n" +
                "rumplestilzken\n" +
                "vddCore\n" +
                "runoono\n" +
                "TechEdison\n" +
                "...and everyone else on the unihertz Titan discord server!\n" +
                "\n" +
                "Thanks to modulizer for github.com/modularizer/HelloWorldKeyboard!\n\n" +
                "v1.3 - github.com/chonkytype")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
    private fun showCharacterPickerDialog(buttonIndex: Int) {
        val characters = arrayOf(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "ä", "ö", "ü", "ß", "à", "è", "é", "ì", "ò", "ù", "â", "ê", "î", "ô", "û", "å", "ø", "æ", "œ", "ç", "ñ", "ã", "õ", "ł", "ś", "ź", "ć", "ę", "ą", "ø", "đ", "þ", "ð", "š", "ž", "ý", "í", "ě", "ř", "ů", "ğ", "ı", "ş", "ħ", "ż", "á", "ú", "¡", "¿", "ñ", "ç", "õ", "à", "ē", "ī", "ū", "ō", "ņ", "ķ", "ļ", "č", "ž", "ŗ", "ğ", "ş", "ė", "į", "ų", "ā", "ē", "ī", "ū", "ļ", "ņ", "č", "š", "ž", "ł", "ś", "ź", "ç", "ř", "ž", "ď", "ť", "ň", "â", "ê", "î", "ô", "û", "ä", "ë", "ï", "ö", "ü", "ÿ", "ā", "ē", "ī", "ō", "ū", "ă", "ĕ", "ĭ", "ŏ", "ŭ",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "!", "?", "§", "$", "%", "&", "/", "(", ")", "=", "`", "´",
            "+", "*", "'", "^", "#", "<", ">", "|", ",", ";", ":", "_", "-", ".", "@", "€", "~", "{", "}", "[", "]", "\\", "\uD83D\uDE00",""
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose new letter")
        builder.setItems(characters) { _, which ->
            updateButtonLabel(buttonIndex, characters[which])
        }
        builder.show()
    }

    private fun showAltCharacterPickerDialog(buttonIndex: Int) {
        val characters = arrayOf(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "ä", "ö", "ü", "ß", "à", "è", "é", "ì", "ò", "ù", "â", "ê", "î", "ô", "û", "å", "ø", "æ", "œ", "ç", "ñ", "ã", "õ", "ł", "ś", "ź", "ć", "ę", "ą", "ø", "đ", "þ", "ð", "š", "ž", "ý", "í", "ě", "ř", "ů", "ğ", "ı", "ş", "ħ", "ż", "á", "ú", "¡", "¿", "ñ", "ç", "õ", "à", "ē", "ī", "ū", "ō", "ņ", "ķ", "ļ", "č", "ž", "ŗ", "ğ", "ş", "ė", "į", "ų", "ā", "ē", "ī", "ū", "ļ", "ņ", "č", "š", "ž", "ł", "ś", "ź", "ç", "ř", "ž", "ď", "ť", "ň", "â", "ê", "î", "ô", "û", "ä", "ë", "ï", "ö", "ü", "ÿ", "ā", "ē", "ī", "ō", "ū", "ă", "ĕ", "ĭ", "ŏ", "ŭ",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "!", "?", "§", "$", "%", "&", "/", "(", ")", "=", "`", "´",
            "+", "*", "'", "^", "#", "<", ">", "|", ",", ";", ":", "_", "-", ".", "@", "€", "~", "{", "}", "[", "]", "\\", "\uD83D\uDE00",""
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose new letter for ALT")
        builder.setItems(characters) { _, which ->
            updateAltButtonLabel(buttonIndex, characters[which])
        }
        builder.show()
    }

    private fun updateAltButtonLabel(buttonIndex: Int, label: String) {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("alt_button_label_${buttonIndex - 10}", label)
            apply()
        }
        val buttonId = when (buttonIndex) {
            11 -> R.id.button11
            12 -> R.id.button12
            13 -> R.id.button13
            14 -> R.id.button14
            15 -> R.id.button15
            16 -> R.id.button16
            17 -> R.id.button17
            18 -> R.id.button18
            19 -> R.id.button19
            20 -> R.id.button20


            else -> 0
        }
        if (buttonId != 0) {
            findViewById<Button>(buttonId).text = label
        }
    }

    private fun updateButtonLabel(buttonIndex: Int, label: String) {
        val sharedPreferences = getSharedPreferences("com.chonkytype.chonkytype_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("button_label_$buttonIndex", label)
            apply()
        }
        val buttonId = when (buttonIndex) {
            1 -> R.id.button1
            2 -> R.id.button2
            3 -> R.id.button3
            4 -> R.id.button4
            5 -> R.id.button5
            6 -> R.id.button6
            7 -> R.id.button7
            8 -> R.id.button8
            9 -> R.id.button9
            10 -> R.id.button10
            else -> 0
        }
        if (buttonId != 0) {
            val displayLabel = if (label == "SS") "ß" else label.toLowerCase(Locale.getDefault())
            findViewById<Button>(buttonId).text = displayLabel
        }
    }





}
