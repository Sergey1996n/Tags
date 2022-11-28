package com.example.tags

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Video.query
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tags.databinding.FragmentFirstBinding
import java.util.concurrent.TimeUnit

private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

class FirstFragment : Fragment() {

    companion object {
        var uri: Uri? = null
    }

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uri?.let { insertData(it) }

        binding.apply {
//            addBtn.setOnClickListener { openFile(null) }
//            addBtn.setOnClickListener { openMediaStore() }
            addBtn.setOnClickListener {
                findNavController().navigate(FirstFragmentDirections.actionFirstFragmentToFragmentViewModel())
            }
            editBtn.setOnClickListener {
                val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(uri!!)
                findNavController().navigate(action)
            }
        }
    }

    private fun insertData(uri: Uri) {
        val contentResolver = requireContext().contentResolver

        binding.apply {
            imageView.setImageURI(uri)
            val inputStream = contentResolver.openInputStream(uri)!!
            val exif = ExifInterface(inputStream)
            dateCreation.text =
                "Date created: " + returnData(exif.getAttribute(ExifInterface.TAG_DATETIME))
            latitudeCoordinate.text =
                "Latitude: " + returnCoordinate(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)) + " " +
                        (exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) ?: "")
            longitudeCoordinate.text =
                "Longitude: " + returnCoordinate(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)) + " " +
                        (exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) ?: "")
            device.text = "Device: " + returnData(exif.getAttribute(ExifInterface.TAG_MAKE))
            model.text = "Model: " + returnData(exif.getAttribute(ExifInterface.TAG_MODEL))
            inputStream.close()
            content.visibility = View.VISIBLE
            editBtn.visibility = View.VISIBLE
        }
    }

    private fun returnCoordinate(attribute: String?): String {
        return if (attribute == null) {
            "No data"
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
        return attribute ?: "No data"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}