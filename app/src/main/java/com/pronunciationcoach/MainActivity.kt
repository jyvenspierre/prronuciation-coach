package com.pronunciationcoach

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale

data class PracticeWord(val word: String, val stress: String, val sentence: String, val category: String)
private val words = listOf(
    PracticeWord("testimony", "TES-ti-mo-ny", "I have a testimony to share.", "Stress"),
    PracticeWord("photography", "pho-TOG-ra-phy", "I'm interested in photography.", "Stress"),
    PracticeWord("development", "de-VEL-op-ment", "I'm working on my development.", "Stress"),
    PracticeWord("comfortable", "COMF-ter-ble", "I feel comfortable speaking English.", "Stress"),
    PracticeWord("really", "REAL-ly", "I really enjoy learning.", "R"),
    PracticeWord("around", "a-ROUND", "I'll be around tomorrow.", "R"),
    PracticeWord("three", "THREE", "I have three questions.", "TH"),
    PracticeWord("think", "THINK", "I think that's a good idea.", "TH"),
    PracticeWord("sheep", "SHEEP /iː/", "The sheep is in the field.", "Vowels"),
    PracticeWord("ship", "SHIP /ɪ/", "The ship is leaving.", "Vowels")
)
private val categories = listOf("All", "Stress", "R", "TH", "Vowels")

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recording by mutableStateOf(false)
    private var recordingReady by mutableStateOf(false)
    private var notice by mutableStateOf("")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notice = if (granted) "Microphone ready. Tap Record again." else "Microphone permission was not granted."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.9f)
            }
        }
        setContent {
            MaterialTheme {
                var category by rememberSaveable { mutableStateOf("All") }
                var index by rememberSaveable { mutableIntStateOf(0) }
                var inSentence by rememberSaveable { mutableStateOf(false) }
                val filtered = words.filter { category == "All" || it.category == category }
                val current = filtered[index % filtered.size]
                val target = if (inSentence) current.sentence else current.word

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Pronunciation Coach", style = MaterialTheme.typography.headlineMedium)
                    Text("American English · Personal practice", style = MaterialTheme.typography.bodyMedium)
                    Text("Choose a skill:")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.take(3).forEach { skill ->
                            FilterChip(
                                selected = category == skill,
                                onClick = { category = skill; index = 0; inSentence = false; recordingReady = false },
                                label = { Text(skill) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.drop(3).forEach { skill ->
                            FilterChip(
                                selected = category == skill,
                                onClick = { category = skill; index = 0; inSentence = false; recordingReady = false },
                                label = { Text(skill) }
                            )
                        }
                    }
                    Text(if (inSentence) "Sentence practice" else "Word practice", style = MaterialTheme.typography.titleMedium)
                    Text(target, style = MaterialTheme.typography.headlineSmall)
                    if (!inSentence) Text("Stress / sound guide: ${current.stress}")
                    Button(onClick = { speak(target, false) }) { Text("▶ Hear pronunciation") }
                    OutlinedButton(onClick = { speak(target, true) }) { Text("▶ Play slowly") }
                    Button(onClick = {
                        if (recording) stopRecording() else startRecording()
                    }) { Text(if (recording) "■ Stop recording" else "● Record my voice") }
                    if (recordingReady) Text("Recording saved on this device for this session. Pronunciation scoring is not available yet.")
                    if (notice.isNotBlank()) Text(notice)
                    HorizontalDivider()
                    Button(onClick = {
                        inSentence = !inSentence
                        recordingReady = false
                        notice = ""
                    }) { Text(if (inSentence) "← Back to word" else "Practice in a sentence →") }
                    OutlinedButton(onClick = {
                        index++
                        inSentence = false
                        recordingReady = false
                        notice = ""
                    }) { Text("Next word →") }
                    Text(
                        "Prototype: scoring and detailed sound-by-sound feedback require a validated pronunciation assessment integration. No score is invented.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    private fun speak(text: String, slow: Boolean) {
        tts?.setSpeechRate(if (slow) 0.65f else 0.9f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "practice")
    }

    private fun startRecording() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val file = File(cacheDir, "last_practice.m4a")
            recordingFile = file
            @Suppress("DEPRECATION")
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            recording = true
            recordingReady = false
            notice = "Recording…"
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            recording = false
            notice = "Could not start recording. Check microphone permission."
        }
    }

    private fun stopRecording() {
        try {
            recorder?.stop()
            recordingReady = true
            notice = "Recorded successfully."
        } catch (e: Exception) {
            recordingReady = false
            notice = "Recording was too short or failed. Try again."
        } finally {
            recorder?.release()
            recorder = null
            recording = false
        }
    }

    override fun onDestroy() {
        recorder?.release()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
