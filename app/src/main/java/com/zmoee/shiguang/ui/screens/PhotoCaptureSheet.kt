package com.zmoee.shiguang.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.pressScale
import java.io.File
import java.util.concurrent.Executor
import kotlinx.coroutines.launch

@Composable
fun PhotoCaptureSheet(
    contact: Contact,
    onDismiss: () -> Unit,
    onSave: (filePath: String, tag: String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    var tag by rememberSaveable(contact.id) { mutableStateOf(contact.photoTag) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionAsked by rememberSaveable { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var inlineMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionAsked = true
        hasPermission = granted
        if (!granted) {
            inlineMessage = "未获得相机权限。你仍可以先保存联系人，稍后在系统设置中开启相机。"
        }
    }

    fun requestCameraPermission() {
        permissionAsked = true
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        )
        context.startActivity(intent)
    }

    fun closeSheet() {
        focusManager.clearFocus()
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    fun takePhoto() {
        focusManager.clearFocus()
        if (!hasPermission) {
            requestCameraPermission()
            return
        }
        val capture = imageCapture
        if (capture == null) {
            inlineMessage = "相机还在准备中，请稍等片刻"
            return
        }
        isCapturing = true
        inlineMessage = null
        val photoDirectory = File(context.filesDir, "photos").apply { mkdirs() }
        val photoFile = File(photoDirectory, "contact_${contact.id}_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val executor: Executor = ContextCompat.getMainExecutor(context)
        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    val path = photoFile.absolutePath
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onSave(path, tag.trim())
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    photoFile.delete()
                    inlineMessage = "拍照失败，请重试"
                }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetMaxWidth = 412.dp,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(width = 32.dp, height = 4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                ) {}
            }
        },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it.take(24) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标签") },
                placeholder = { Text("例如：老同学、家门口") },
                leadingIcon = {
                    Symbol(
                        symbol = Sym.Search,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        takePhoto()
                    },
                ),
                supportingText = {
                    Text(
                        text = "选填。写一个关键词，之后可以在「搜索」里找到这张照片。",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(380f / 507f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                if (hasPermission) {
                    CameraPreview(
                        onCameraReady = { capture ->
                            imageCapture = capture
                            if (capture != null) cameraError = null
                        },
                        onError = { message ->
                            cameraError = message
                            imageCapture = null
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (cameraError != null) {
                        CameraUnavailablePanel(
                            title = "相机暂不可用",
                            body = cameraError.orEmpty(),
                            actionLabel = "打开系统设置",
                            onAction = { openAppSettings() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    // A subtle preview label helps communicate the live camera state.
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.78f),
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Symbol(symbol = Sym.PhotoCamera, size = 14.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "实时预览",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                } else {
                    CameraUnavailablePanel(
                        title = "相机还未开启",
                        body = "允许相机访问后，就能给 ${contact.name} 拍一张新照片。",
                        actionLabel = "允许相机访问",
                        onAction = { requestCameraPermission() },
                        secondaryActionLabel = "前往系统设置",
                        onSecondaryAction = { openAppSettings() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (inlineMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = inlineMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (!hasPermission && permissionAsked) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "可以随时在系统设置里重新开启相机权限。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = { takePhoto() },
                    modifier = Modifier
                        .width(300.dp)
                        .height(56.dp)
                        .pressScale(interactionSource),
                    enabled = !isCapturing,
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ContentPadding,
                    interactionSource = interactionSource,
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Symbol(symbol = Sym.Add, size = 18.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isCapturing) "正在保存…" else "拍摄并添加")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "照片只会保存在本机",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CameraUnavailablePanel(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val panelColor = MaterialTheme.colorScheme.inverseSurface
    val contentColor = MaterialTheme.colorScheme.inverseOnSurface
    Column(
        modifier = modifier
            .background(panelColor)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Symbol(
            symbol = Sym.PhotoCamera,
            tint = contentColor,
            size = 56.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onAction,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.inversePrimary,
            ),
        ) {
            Text(actionLabel)
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            TextButton(
                onClick = onSecondaryAction,
                colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            ) {
                Text(secondaryActionLabel)
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onCameraReady: (ImageCapture?) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCameraReady by rememberUpdatedState(onCameraReady)
    val currentOnError by rememberUpdatedState(onError)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner) {
        var cameraProvider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        future.addListener(
            {
                try {
                    val provider = future.get()
                    cameraProvider = provider
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    val selector = if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    currentOnCameraReady(capture)
                } catch (error: Exception) {
                    currentOnCameraReady(null)
                    currentOnError("无法启动相机，请确认设备支持相机功能")
                }
            },
            executor,
        )

        onDispose {
            currentOnCameraReady(null)
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}
