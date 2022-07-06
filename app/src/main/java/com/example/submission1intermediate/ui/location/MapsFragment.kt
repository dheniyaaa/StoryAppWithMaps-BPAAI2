package com.example.submission1intermediate.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.submission1intermediate.MainActivity
import com.example.submission1intermediate.R
import com.example.submission1intermediate.databinding.FragmentMapsBinding
import com.example.submission1intermediate.utils.ViewModelFactory
import com.example.submission1intermediate.vstate.State
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MapsFragment : Fragment(), KodeinAware {

    override val kodein: Kodein by kodein()
    private val viewModelFactory: ViewModelFactory by instance()
    private var token: String = ""
    private lateinit var locationViewModel: LocationViewModel
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel= ViewModelProvider(this, viewModelFactory)[LocationViewModel::class.java]
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return  binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.container_in_mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        token = requireActivity().intent.getStringExtra(MainActivity.EXTRA_TOKEN) ?: "token null"


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }


    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        getMyDeviceLocation()
        markStoryLocation()

    }

    private fun getMyDeviceLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null){
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
                } else{
                    Toast.makeText(requireContext(), "Please active your location service", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        if (isGranted){
            getMyDeviceLocation()
        }
    }

    private fun markStoryLocation(){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                locationViewModel.getAllLocationStory(token).observeForever {
                    it.let { resource ->
                        when(resource.status){
                            State.SUCCESS ->{

                                resource.data?.body?.stories?.forEach { story ->
                                    if (story.lat != null && story.lon != null){
                                        val latLng =LatLng(story.lat, story.lon)

                                        mMap.addMarker(
                                            MarkerOptions()
                                                .position(latLng)
                                                .title(story.name)
                                                .snippet("Lat: ${story.lat}, Lon: ${story.lon}")
                                        )
                                    }
                                }

                            }
                            State.ERROR -> {
                                Toast.makeText(context, "Failed get APi", Toast.LENGTH_SHORT).show()

                            }
                        }
                    }
                }
            }
        }
    }

}