package com.instasplit.app.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.instasplit.app.presentation.editor.EditorScreen
import com.instasplit.app.presentation.export.ExportScreen
import com.instasplit.app.presentation.home.HomeScreen
import com.instasplit.app.presentation.preview.PreviewScreen
import kotlinx.serialization.Serializable

// â”€â”€ Routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Serializable object Home
@Serializable data class Editor(val uriString: String)
@Serializable data class Preview(val uriString: String)
@Serializable data class Export(val uriString: String)

// â”€â”€ Graph â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun InstaSplitNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Home
) {
    NavHost(navController, startDestination = startDestination) {
        composable<Home> {
            HomeScreen(
                onImageLoaded = { uri -> 
                    navController.navigate(Editor(uriString = uri.toString())) 
                }
            )
        }
        composable<Editor> { backStackEntry ->
            val route: Editor = backStackEntry.toRoute()
            EditorScreen(
                onBack = { navController.popBackStack() },
                onPreview = { navController.navigate(Preview(uriString = route.uriString)) }
            )
        }
        composable<Preview> { backStackEntry ->
            val route: Preview = backStackEntry.toRoute()
            PreviewScreen(
                onBack = { navController.popBackStack() },
                onExport = { navController.navigate(Export(uriString = route.uriString)) }
            )
        }
        composable<Export> {
            ExportScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

