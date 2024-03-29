package com.getir.patika.foodcouriers

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
        // Kullanıcının mevcut konumunu göster
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // İzinleri kontrol et, eğer izin verilmediyse, iste.
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
            if (location != null) {
                // Haritayı kullanıcının mevcut konumuna odakla ve animasyonlu bir şekilde zoom yap
                getAddressFromLocation(location)

                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.addMarker(MarkerOptions().position(userLocation).title("Here"))

                val circleOptions = CircleOptions()
                    .center(userLocation) // Çemberin merkezi
                    .radius(location.accuracy.toDouble()) // Çemberin yarıçapı, konumun doğruluğuna bağlı
                    .fillColor(Color.parseColor("#80F2A2AE")) // Çemberin iç rengi, burada %20 opaklıkta mavi
                    .strokeColor(Color.RED) // Çemberin kenarlık rengi
                    .strokeWidth(2f) // Çemberin kenarlık kalınlığı

                mMap.addCircle(circleOptions)

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 17f)
                // Kamera animasyonunu başlat
                mMap.animateCamera(cameraUpdate, 4000, object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        binding.backButton.visibility = View.VISIBLE
                        binding.notificationButton.visibility = View.VISIBLE

                        binding.backButton.setOnClickListener {
                            findNavController().navigate(R.id.action_setLocation2_to_createAccountFragment)
                        }
                    }

                    override fun onCancel() {
                        // Animasyon iptal edildiğinde çağrılır
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
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi, konum güncellemesini etkinleştir
                updateLocationUI()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                // Burada elde edilen adresi kullanın. Örneğin:
                val addressStr = address.getAddressLine(0)
                // Adres bilgisini bir TextView'a veya başka bir arayüz elemanına ayarlayın.
                updateUIWithAddress(addressStr)
            } else {
                // Adres bulunamadı
            }
        } catch (e: IOException) {
            // Geocoder servisine ulaşılamadı
        }
    }

    fun updateUIWithAddress(address: String) {
        addressEditText.setText(address)
    }


}



