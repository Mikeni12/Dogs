package com.example.dogs.view

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.dogs.R
import com.example.dogs.databinding.DialogSendSmsBinding
import com.example.dogs.databinding.FragmentDetailBinding
import com.example.dogs.model.DogBreed
import com.example.dogs.model.DogPalette
import com.example.dogs.model.SmsInfo
import com.example.dogs.viewmodel.DetailViewModel

class DetailFragment : Fragment() {

    private lateinit var viewModel: DetailViewModel
    private var dogUuid = 0

    private lateinit var dataBinding: FragmentDetailBinding
    private lateinit var currentDog: DogBreed
    private var sendSmsStarted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        viewModel.fetch(dogUuid)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dogLiveData.observe(this, Observer { dogBreed ->
            dogBreed?.let { dog ->
                currentDog = dog
                dataBinding.dog = dog
                dog.imageUrl?.let {
                    setUpBackgroundColor(it)
                }
            }
        })
    }

    private fun setUpBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette ->
                        val intColor = palette?.mutedSwatch?.rgb ?: 0
                        val mPalette = DogPalette(intColor)
                        dataBinding.palette = mPalette
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                (activity as MainActivity).checkSmsPermission()
            }
            R.id.action_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Mira esta raza de perro")
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${currentDog.dogBreed} criado para ${currentDog.bredFor}"
                )
                intent.putExtra(Intent.EXTRA_STREAM, currentDog.imageUrl)
                startActivity(Intent.createChooser(intent, "Compartir con"))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                val sms = SmsInfo(
                    "",
                    "${currentDog.dogBreed} criado para ${currentDog.bredFor}",
                    currentDog.imageUrl
                )

                val dialogBinding = DataBindingUtil.inflate<DialogSendSmsBinding>(
                    LayoutInflater.from(it),
                    R.layout.dialog_send_sms,
                    null,
                    false
                )

                AlertDialog.Builder(it)
                    .setView(dialogBinding.root)
                    .setPositiveButton("Enviar SMS") { _, _ ->
                        if (!dialogBinding.edtSmsDestination.text.isNullOrEmpty()) {
                            sms.to = dialogBinding.edtSmsDestination.text.toString()
                            sendSms(sms)
                        }
                    }
                    .setNegativeButton("Cancelar") { _, _ -> }
                    .show()

                dialogBinding.smsInfo = sms
            }
        }
    }

    private fun sendSms(sms: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(sms.to, null, sms.text, pendingIntent, null)
    }
}
