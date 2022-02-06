package com.jetpack.periodicworkmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.work.*
import com.jetpack.periodicworkmanager.ui.theme.PeriodicWorkManagerTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var worker: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeriodicWorkManagerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    worker = WorkManager.getInstance()
                    val textInput = remember { mutableStateOf("") }
                    val powerConstraints = Constraints.Builder().setRequiresCharging(true).build()
                    val taskData = Data.Builder().putString(MESSAGE_STATUS, "Notification Done.").build()
                    val work = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.MINUTES)
                        .setConstraints(powerConstraints)
                        .setInputData(taskData)
                        .build()
                    worker.enqueueUniquePeriodicWork("Notify", ExistingPeriodicWorkPolicy.KEEP, work)

                    worker.getWorkInfoByIdLiveData(work.id).observe(this, Observer { workInfo ->
                        workInfo.let {
                            if (it.state.isFinished) {
                                val outputData = it.outputData
                                val taskResult = outputData.getString(NotificationWorker.WORK_RESULT)
                                if (taskResult != null) {
                                    textInput.value = taskResult
                                }
                            } else {
                                val workStatus = workInfo.state
                                textInput.value = workStatus.toString()
                            }
                        }
                    })

                    Scaffold(
                      topBar =  {
                          TopAppBar(
                            title = {
                                Text(
                                    text = "Periodic Work Manager",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                          )
                      }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { worker.enqueue(work) }) {
                                Text(text = "Submit")
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(text = textInput.value)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MESSAGE_STATUS = "message_status"
    }
}


















