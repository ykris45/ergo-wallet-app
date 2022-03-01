package org.ergoplatform.android.tokens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.ergoplatform.TokenAmount
import org.ergoplatform.WalletStateSyncManager
import org.ergoplatform.android.Preferences
import org.ergoplatform.android.R
import org.ergoplatform.android.databinding.FragmentTokenInformationBinding
import org.ergoplatform.android.ui.AndroidStringProvider
import org.ergoplatform.android.ui.openUrlWithBrowser
import org.ergoplatform.getExplorerTokenUrl
import org.ergoplatform.getExplorerTxUrl
import org.ergoplatform.tokens.isSingularToken
import org.ergoplatform.utils.formatTokenPriceToString

class TokenInformationDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentTokenInformationBinding? = null
    private val binding get() = _binding!!

    private val args: TokenInformationDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTokenInformationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDone.setOnClickListener { buttonDone() }

        val viewModel: TokenInformationViewModel by viewModels()

        viewModel.init(args.tokenId, requireContext())

        viewModel.tokenInfo.observe(viewLifecycleOwner) { token ->
            binding.progressCircular.visibility = View.GONE
            token?.apply {
                binding.mainLayout.visibility = View.VISIBLE
                binding.labelTokenName.text =
                    if (displayName.isBlank()) getString(R.string.label_unnamed_token) else displayName

                binding.labelTokenName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, getGenuineDrawableId(), 0
                )

                binding.labelTokenId.text = tokenId
                binding.labelTokenDescription.text =
                    if (description.isNotBlank()) description else getString(R.string.label_no_description)
                binding.labelSupplyAmount.text =
                    TokenAmount(fullSupply, decimals).toStringUsFormatted(false)
                val balanceAmount = TokenAmount(args.amount, decimals)
                binding.labelBalanceAmount.text =
                    balanceAmount.toStringUsFormatted(false)

                val showBalance = args.amount > 0 && !isSingularToken()
                val walletSyncManager = WalletStateSyncManager.getInstance()
                val tokenPrice = walletSyncManager.tokenPrices[tokenId]

                if (showBalance) tokenPrice?.let {
                    binding.labelBalanceValue.text = formatTokenPriceToString(
                        balanceAmount,
                        it.ergValue,
                        walletSyncManager,
                        AndroidStringProvider(requireContext())
                    ) + " [${it.priceSource}]"
                }

                binding.labelBalanceAmount.visibility = if (showBalance) View.VISIBLE else View.GONE
                binding.titleBalanceAmount.visibility = binding.labelBalanceAmount.visibility
                binding.labelBalanceValue.visibility =
                    if (showBalance && tokenPrice != null) View.VISIBLE else View.GONE
                binding.labelSupplyAmount.visibility =
                    if (isSingularToken()) View.GONE else View.VISIBLE
                binding.titleSupplyAmount.visibility = binding.labelSupplyAmount.visibility

                binding.labelMintingTxId.setOnClickListener {
                    openUrlWithBrowser(requireContext(), getExplorerTxUrl(mintingTxId))
                }

                updateNftLayout(viewModel)

                binding.buttonDownloadContent.setOnClickListener {
                    viewModel.uiLogic.downloadContent(
                        Preferences(requireContext())
                    )
                }
            } ?: run { binding.tvError.visibility = View.VISIBLE }
        }

        viewModel.downloadState.observe(viewLifecycleOwner) {
            updateNftLayout(viewModel, true)
        }

        binding.labelTokenId.setOnClickListener {
            openUrlWithBrowser(
                requireContext(),
                getExplorerTokenUrl(binding.labelTokenId.text.toString())
            )
        }
        binding.labelTokenDescription.setOnClickListener {
            // XML animateLayoutChanges does not work in BottomSheet
            TransitionManager.beginDelayedTransition(binding.mainLayout)
            binding.labelTokenDescription.maxLines =
                if (binding.labelTokenDescription.maxLines == 5) 1000 else 5
        }
    }

    private fun updateNftLayout(
        viewModel: TokenInformationViewModel,
        onlyPreview: Boolean = false
    ) {
        val context = requireContext()
        val tokenInformationNftLayoutView = TokenInformationNftLayoutView(binding)
        if (onlyPreview) {
            tokenInformationNftLayoutView.updatePreview(viewModel.uiLogic)
        } else {
            tokenInformationNftLayoutView.update(
                viewModel.uiLogic,
                AndroidStringProvider(context),
                Preferences(context)
            ) { openUrlWithBrowser(context, it) }
        }
    }

    private fun buttonDone() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}