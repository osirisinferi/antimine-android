package dev.lucasnlm.antimine.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CustomEvent

@AndroidEntryPoint
class CustomLevelDialogFragment : AppCompatDialogFragment() {
    private val viewModel by activityViewModels<GameViewModel>()
    private val createGameViewModel by activityViewModels<CreateGameViewModel>()

    private lateinit var mapWidth: TextView
    private lateinit var mapHeight: TextView
    private lateinit var mapMines: TextView

    private fun getSelectedMinefield(): Minefield {
        val width = filterInput(mapWidth.text.toString(), MIN_WIDTH).coerceAtMost(MAX_WIDTH)
        val height = filterInput(mapHeight.text.toString(), MIN_HEIGHT).coerceAtMost(MAX_HEIGHT)
        val mines = filterInput(mapMines.text.toString(), MIN_MINES).coerceAtMost(width * height - 1)

        return Minefield(width, height, mines)
    }

    @SuppressLint("InflateParams")
    private fun createView(): View {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_custom_game, null, false)

        mapWidth = view.findViewById(R.id.map_width)
        mapHeight = view.findViewById(R.id.map_height)
        mapMines = view.findViewById(R.id.map_mines)

        createGameViewModel.singleState().let {
            mapWidth.text = it.width.toString()
            mapHeight.text = it.height.toString()
            mapMines.text = it.mines.toString()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.new_game)
            setView(createView())
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.start) { _, _ ->
                val minefield = getSelectedMinefield()
                createGameViewModel.sendEvent(CustomEvent.UpdateCustomGameEvent(minefield))
                viewModel.startNewGame(Difficulty.Custom)
            }
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
        super.onDismiss(dialog)
    }

    companion object {
        const val MIN_WIDTH = 5
        const val MIN_HEIGHT = 5
        const val MIN_MINES = 3

        const val MAX_WIDTH = 50
        const val MAX_HEIGHT = 50

        private fun filterInput(target: String, min: Int): Int {
            var result = min

            try {
                result = Integer.valueOf(target)
            } catch (e: NumberFormatException) {
                result = min
            } finally {
                result = result.coerceAtLeast(min)
            }

            return result
        }

        val TAG = CustomLevelDialogFragment::class.simpleName!!
    }
}
