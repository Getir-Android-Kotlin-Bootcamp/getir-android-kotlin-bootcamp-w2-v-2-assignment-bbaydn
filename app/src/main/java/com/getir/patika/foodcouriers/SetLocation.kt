package com.getir.patika.foodcouriers

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.getir.patika.foodcouriers.databinding.FragmentSetLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

class SetLocation : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var addressEditText: EditText
    private lateinit var binding: FragmentSetLocationBinding


    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
            if (location != null) {
                getAddressFromLocation(location)

                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.addMarker(MarkerOptions().position(userLocation).title("Here"))

                val circleOptions = CircleOptions()
                    .center(userLocation)
                    .radius(location.accuracy.toDouble())
                    .fillColor(Color.parseColor("#80F2A2AE"))
                    .strokeColor(Color.RED)
                    .strokeWidth(2f)

                mMap.addCircle(circleOptions)

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 17f)
                mMap.animateCamera(cameraUpdate, 4000, object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        binding.backButton.setOnClickListener {
                            findNavController().navigate(R.id.action_setLocation2_to_createAccountFragment)
                        }
                        binding.notificationButton.visibility = View.VISIBLE
                        binding.cardSearch.root.visibility = View.VISIBLE
                        binding.backButton.visibility = View.VISIBLE
                    }

                    override fun onCancel() {
                        Toast.makeText(requireContext(), "Animation cancelled!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding = FragmentSetLocationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressEditText = view.findViewById(R.id.addressEditText)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        binding.notificationButton.visibility = View.GONE
        binding.cardSearch.root.visibility = View.GONE
        binding.backButton.visibility = View.GONE

        val searchButton = binding.cardSearch.searchButton
        val searchText = binding.cardSearch.searchText


        searchButton.setOnClickListener {
            val location = searchText.text.toString()
            if (location.isNotEmpty()) {
                searchLocation(location)
            }
        }
    }

    private fun searchLocation(location: String) {
        Geocoder(requireContext(), Locale.getDefault()).run {
            try {
                getFromLocationName(location, 1)?.firstOrNull()?.let { address ->
                    LatLng(address.latitude, address.longitude).also { latLng ->
                        mMap.addMarker(MarkerOptions().position(latLng).title(location))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        binding.cardAddress.addressEditText.setText(address.getAddressLine(0))
                    }
                } ?: run {
                    Log.d("searchLocation", "No address found for the location.")
                    Toast.makeText(requireContext(), "No address found for the location.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Log.e("searchLocation", "Geocoder failed", e)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationUI()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val addressStr = address.getAddressLine(0)
                updateUIWithAddress(addressStr)
            } else {
                Toast.makeText(requireContext(), "Address couldn't find!", Toast.LENGTH_SHORT).show()

            }
        } catch (e: IOException) {
            Log.e("IOException", e.toString())
        }
    }

    private fun updateUIWithAddress(address: String) {
        addressEditText.setText(address)
    }


}



