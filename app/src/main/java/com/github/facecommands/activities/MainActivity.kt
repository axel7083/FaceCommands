package com.github.facecommands.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.facecommands.Utils
import com.github.facecommands.services.FloatingViewService
import com.github.facecommands.Utils.isAccessServiceEnabled
import com.github.facecommands.databinding.ActivityMainBinding
import com.github.facecommands.services.AutoService


class MainActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    //FrameLayout mLayout;


    companion object {
        private const val SYSTEM_ALERT_WINDOW_PERMISSION = 2084
        private const val RC_HANDLE_CAMERA_PERM = 2
        private const val RC_HANDLE_ACCESSIBILITY = 3
        private const val RC_HANDLE_SETTINGS = 4
        public const val MY_PREFS = "faceCommands"
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private var rightEyeCommand: Int = -1
    private var leftEyeCommand: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()

        // Setup switch
        refreshStartBtn()


        // Setup spinner
        if(rightEyeCommand == -1 && leftEyeCommand == -1) {
            rightEyeCommand = 0
            leftEyeCommand = 1

            saveSettings()
        }

        binding.rightEyeSpinner.setSelection(rightEyeCommand)
        binding.leftEyeSpinner.setSelection(leftEyeCommand)


        // Set listener
        binding.appearOnTopSwitch.setOnCheckedChangeListener(this)
        binding.cameraSwitch.setOnCheckedChangeListener(this)
        binding.accessibilitySwitch.setOnCheckedChangeListener(this)

        binding.rightEyeSpinner.onItemSelectedListener = this
        binding.leftEyeSpinner.onItemSelectedListener = this

        binding.startFloat.setOnClickListener(this)
    }

    private fun saveSettings() {
        val editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit()

        editor.putInt("rightEyeCommand",rightEyeCommand);
        editor.putInt("leftEyeCommand",leftEyeCommand);

        editor.apply()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE)

        rightEyeCommand = prefs.getInt("rightEyeCommand",-1);
        leftEyeCommand = prefs.getInt("leftEyeCommand",-1);
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(p0 == null)
            return;

        if(p0.id == binding.rightEyeSpinner.id) {
            rightEyeCommand = p2
            Log.d(TAG,"[rightEyeSpinner] p2: $p2 p3: $p3")
        }
        else  if(p0.id == binding.leftEyeSpinner.id)
        {
            leftEyeCommand = p2
            Log.d(TAG,"[leftEyeSpinner] p2: $p2 p3: $p3")
        }

        saveSettings()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // Do nothing
    }


    private fun askPermission() {
        val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
    }


    override fun onClick(v: View?) {

        if(v == null)
            return

        if(v.id == binding.startFloat.id ) {
            if(Utils.isServiceRunning(FloatingViewService::class.java, this)) {
                binding.startFloat.text = "Already running"
                binding.startFloat.isEnabled = false
                return
            }
            else
            {
                startService(Intent(this@MainActivity, FloatingViewService::class.java))
                finish()
            }
        }
    }

    private fun requestCameraPermission() {
        Log.w(
                TAG,
                "Camera permission is not granted. Requesting permission"
        )
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(
                this,
                permissions,
                RC_HANDLE_CAMERA_PERM
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(
                    TAG,
                    "Got unexpected permission result: $requestCode"
            )
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(
                    TAG,
                    "Camera permission granted - initialize the camera source"
            )
            // we have permission
            refreshStartBtn()
            return
        }
        Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        refreshStartBtn()
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {

        if(p0 == null)
            return;

        if(p0.id == binding.appearOnTopSwitch.id) {
            if(p1)
                askPermission()
            else
                openSettings()
        }
        else if(p0.id == binding.cameraSwitch.id) {
            if(p1)
                requestCameraPermission()
            else
                openSettings()
        }
        else if(p0.id == binding.accessibilitySwitch.id) {
            startActivityForResult(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                RC_HANDLE_ACCESSIBILITY
            )
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, RC_HANDLE_SETTINGS)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_HANDLE_ACCESSIBILITY) {
            if (resultCode == RESULT_OK) {
                refreshStartBtn()
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG,"RC_HANDLE_ACCESSIBILITY cancel")
                refreshStartBtn()

                if(!binding.accessibilitySwitch.isChecked)
                    Toast.makeText(this,"You need to activate the \"Blink to scroll\" service to use this application.",Toast.LENGTH_LONG).show()
            }
        }
        else if(requestCode == RC_HANDLE_SETTINGS) {
            refreshStartBtn()
        }
    } //onActivityResult



    private fun refreshSwitches() {
        binding.appearOnTopSwitch.setOnCheckedChangeListener(null)
        binding.cameraSwitch.setOnCheckedChangeListener(null)
        binding.accessibilitySwitch.setOnCheckedChangeListener(null)

        binding.appearOnTopSwitch.isChecked = Settings.canDrawOverlays(this)
        binding.cameraSwitch.isChecked = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        binding.accessibilitySwitch.isChecked = isAccessServiceEnabled(
                this,
                AutoService::class.java
        )

        binding.appearOnTopSwitch.setOnCheckedChangeListener(this)
        binding.cameraSwitch.setOnCheckedChangeListener(this)
        binding.accessibilitySwitch.setOnCheckedChangeListener(this)
    }

    private fun checkPermissions() : Boolean {
        refreshSwitches()
        return binding.appearOnTopSwitch.isChecked  && binding.cameraSwitch.isChecked && binding.accessibilitySwitch.isChecked
    }

    private fun refreshStartBtn() {

        if(checkPermissions()) {
            binding.startFloat.text = "Start"
            binding.startFloat.isEnabled = true
        }
        else
        {
            binding.startFloat.text = "Start"
            binding.startFloat.isEnabled = false
        }

    }
}