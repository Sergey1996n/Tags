package com.example.tags

import android.app.Activity
import android.app.RecoverableSecurityException
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tags.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: SecondFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentResolver = requireContext().contentResolver
        val fileDescriptor = contentResolver.openFileDescriptor(navigationArgs.uri, "r", null)!!
        val exif = ExifInterface(fileDescriptor.fileDescriptor)
        binding.apply {
            dateCreatedInput.setText(returnData(exif.getAttribute(ExifInterface.TAG_DATETIME)))
            latitudeCoordinateInput.setText(returnCoordinate(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)) + " " +
                    (exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) ?: ""))
            longitudeCoordinateInput.setText(returnCoordinate(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)) + " " +
                    (exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) ?: ""))
            deviceInput.setText(returnData(exif.getAttribute(ExifInterface.TAG_MAKE)))
            modelInput.setText(returnData(exif.getAttribute(ExifInterface.TAG_MODEL)))
            fileDescriptor.close()
        }
        binding.saveBtn.setOnClickListener {
            try {
                saveData()
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                    requestUri.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    throw securityException
                }
            }
        }
    }

    private var requestUri = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result != null && result.resultCode == Activity.RESULT_OK) {
            saveData()
        }
    }

    private fun saveData() {
        val contentResolver = requireContext().contentResolver
        val fileDescriptor = contentResolver.openFileDescriptor(navigationArgs.uri, "rw", null)!!
        val exif = ExifInterface(fileDescriptor.fileDescriptor)
        binding.apply {
            exif.setAttribute(ExifInterface.TAG_DATETIME, dateCreatedInput.text.toString())
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, saveCoordinate(latitudeCoordinateInput.text.toString())[0])
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, saveCoordinate(latitudeCoordinateInput.text.toString())[1])
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, saveCoordinate(longitudeCoordinateInput.text.toString())[0])
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, saveCoordinate(longitudeCoordinateInput.text.toString())[1])
            exif.setAttribute(ExifInterface.TAG_MAKE, deviceInput.text.toString())
            exif.setAttribute(ExifInterface.TAG_MODEL, modelInput.text.toString())
        }
        exif.saveAttributes()
        fileDescriptor.close()
        findNavController().navigate(SecondFragmentDirections.actionSecondFragmentToFirstFragment())
    }

    private fun saveCoordinate(attribute: String): List<String> {
        return if (attribute.isBlank()) {
            listOf("", "")
        } else {
            val angel = attribute.substring(0 until attribute.indexOf('°'))
            val minute = attribute.substring(attribute.indexOf('°') + 1 until attribute.indexOf('\''))
            val second = attribute.substring(attribute.indexOf('\'') + 1 until attribute.indexOf('\"'))
            listOf("$angel/1,$minute/1,$second/10000", attribute.substring(attribute.length - 1))
        }

    }

    private fun returnCoordinate(attribute: String?): String {
        return if (attribute == null) {
            ""
        } else {
            val coor = attribute.split("/1,")
            val angel = coor[0]
            val minute = coor[1]
            val secCoor = coor[2].split("/")
            val second = secCoor[0].toDouble() / secCoor[1].toDouble()
            "$angel°${minute}'${"%.1f".format(second).replace(",", ".")}\""
        }
    }

    private fun returnData(attribute: String?): String {
        return attribute ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}