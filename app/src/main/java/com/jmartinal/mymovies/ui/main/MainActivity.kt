package com.jmartinal.mymovies.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.jmartinal.domain.Movie
import com.jmartinal.mymovies.Constants
import com.jmartinal.mymovies.R
import com.jmartinal.mymovies.data.AndroidPermissionManager
import com.jmartinal.mymovies.databinding.ActivityMainBinding
import com.jmartinal.mymovies.ui.detail.MovieDetailActivity
import com.jmartinal.mymovies.ui.main.MainUIError.GenericError
import com.jmartinal.mymovies.ui.main.MainUIError.NetworkError
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by currentScope.viewModel(this)
    private val adapter by lazy { MoviesAdapter(viewModel::onMovieClicked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        showProgress()

        moviesList.adapter = adapter
        viewModel.error.observe(this, Observer(::showError))
        viewModel.moviesList.observe(this, Observer(::showMovies))
        viewModel.navigateToDetails.observe(this, Observer(::navigateTo))
        viewModel.requestPermission.observe(this, Observer {
            val permissionManager = AndroidPermissionManager(application, this@MainActivity)
            permissionManager.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {continuation ->
                if (continuation) {
                    viewModel.onPermissionGranted()
                } else {
                    viewModel.onPermissionDenied()
                }
            }
        })
    }

    private fun showProgress() {
        moviesList.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        moviesList.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

    private fun showMovies(movies: List<Movie>) {
        hideProgress()
        adapter.movies = movies
        adapter.notifyDataSetChanged()
    }

    private fun navigateTo(movie: Movie) {
        Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
            putExtra(Constants.Communication.KEY_MOVIE, movie.id)
            startActivity(this)
        }
    }

    private fun showError(error: MainUIError) {
        hideProgress()
        val message = when (error) {
            GenericError -> getString(R.string.generic_error)
            NetworkError -> getString(R.string.no_connectivity_error)
        }
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.error_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    finish()
                }
                .create()
                .show()
        }
    }

}
