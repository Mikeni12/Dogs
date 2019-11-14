package com.example.dogs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.dogs.R
import com.example.dogs.databinding.ItemDogBinding
import com.example.dogs.model.DogBreed
import kotlinx.android.synthetic.main.item_dog.view.*

class DogsListAdapter(val dogs: ArrayList<DogBreed>) : DogClickListener,
    RecyclerView.Adapter<DogsListAdapter.DogViewHolder>() {

    fun updateDogsList(newDogs: List<DogBreed>) {
        dogs.clear()
        dogs.addAll(newDogs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil
            .inflate<ItemDogBinding>(inflater, R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun getItemCount(): Int = dogs.size

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.view.dog = dogs[position]
        holder.view.listener = this
    }

    override fun onDogClicked(view: View) {
        val uuid = view.tvDogId.text.toString().toInt()
        val action = ListFragmentDirections.actionDetailFragment()
        action.dogUuid = uuid
        Navigation.findNavController(view).navigate(action)
    }

    inner class DogViewHolder(var view: ItemDogBinding) : RecyclerView.ViewHolder(view.root)
}