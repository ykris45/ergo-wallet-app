package org.ergoplatform.android.mosaik

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ergoplatform.android.AppDatabase
import org.ergoplatform.android.databinding.FragmentAppOverviewBinding
import org.ergoplatform.android.databinding.FragmentAppOverviewItemBinding
import org.ergoplatform.android.ui.hideForcedSoftKeyboard
import org.ergoplatform.android.ui.navigateSafe
import org.ergoplatform.mosaik.MosaikAppEntry

class AppOverviewFragment : Fragment() {
    private var _binding: FragmentAppOverviewBinding? = null
    private val binding get() = _binding!!

    // TODO Mosaik way to delete mosaik_host entries and non-favorite apps

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.inputAppUrl.setEndIconOnClickListener { navigateToApp() }
        binding.inputAppUrl.editText?.setOnEditorActionListener { _, _, _ ->
            navigateToApp()
            true
        }

        fillAppLists()
    }

    private fun fillAppLists() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val lastVisited = withContext(Dispatchers.IO) {
                val lastVisited = db.mosaikDbProvider.getAllAppsByLastVisited(5)
                lastVisited.lastOrNull()?.lastVisited?.let { oldestShownEntry ->
                    db.mosaikDbProvider.deleteAppsNotFavoriteVisitedBefore(oldestShownEntry)
                }

                lastVisited
            }
            val favorites = withContext(Dispatchers.IO) {
                db.mosaikDbProvider.getAllAppFavorites().sortedBy { it.name.lowercase() }
            }

            binding.descEmpty.visibility =
                if (lastVisited.isEmpty() && favorites.isEmpty()) View.VISIBLE else View.GONE
            binding.descFavoritesEmpty.visibility =
                if (favorites.isEmpty()) View.VISIBLE else View.GONE
            binding.descLastVisitedEmpty.visibility =
                if (lastVisited.isEmpty()) View.VISIBLE else View.GONE

            binding.layoutFavorites.apply {
                removeAllViews()
                favorites.forEach { addAppEntry(this, it) }
            }

            binding.layoutLastVisited.apply {
                removeAllViews()
                lastVisited.forEach { addAppEntry(this, it) }
            }
        }

    }

    private fun addAppEntry(linearLayout: LinearLayout, mosaikApp: MosaikAppEntry) {
        val binding = FragmentAppOverviewItemBinding.inflate(layoutInflater, linearLayout, true)
        binding.labelAppTitle.text = mosaikApp.name
        binding.labelAppDesc.text =
            if (mosaikApp.description.isNullOrBlank()) mosaikApp.url else mosaikApp.description
        binding.labelAppDesc.maxLines = if (mosaikApp.description.isNullOrBlank()) 1 else 3
        binding.root.setOnClickListener { navigateToApp(mosaikApp.url) }
    }

    private fun navigateToApp() {
        val url = binding.inputAppUrl.editText?.text.toString()

        if (url.isNotBlank()) {
            navigateToApp(url)
        }
    }

    private fun navigateToApp(url: String) {
        hideForcedSoftKeyboard(requireContext(), binding.inputAppUrl.editText!!)
        findNavController().navigateSafe(
            AppOverviewFragmentDirections.actionAppOverviewFragmentToMosaik(
                url
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}