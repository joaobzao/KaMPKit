package co.touchlab.kampkit

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SqlDelightTest {

    private lateinit var dbHelper: DatabaseHelper

    private suspend fun DatabaseHelper.insertBreed(name: String) {
        insertBreeds(listOf(name))
    }

    @BeforeTest
    fun setup() = runTest {
        dbHelper = DatabaseHelper(
            testDbConnection(),
            Logger,
            Dispatchers.Default
        )
        dbHelper.deleteAll()
        dbHelper.insertBreed("Beagle")
    }

    @Test
    fun `Select All Items Success`() = runTest {
        val breeds = dbHelper.selectAllItems().first()
        assertNotNull(
            breeds.find { it.name == "Beagle" },
            "Could not retrieve Breed"
        )
    }

    @Test
    fun `Select Item by Id Success`() = runTest {
        val breeds = dbHelper.selectAllItems().first()
        val firstBreed = breeds.first()
        assertNotNull(
            dbHelper.selectById(firstBreed.id),
            "Could not retrieve Breed by Id"
        )
    }

    @Test
    fun `Update Favorite Success`() = runTest {
        val breeds = dbHelper.selectAllItems().first()
        val firstBreed = breeds.first()
        dbHelper.updateFavorite(firstBreed.id, true)
        val newBreed = dbHelper.selectById(firstBreed.id).first().first()
        assertNotNull(
            newBreed,
            "Could not retrieve Breed by Id"
        )
        assertTrue(
            newBreed.favorite,
            "Favorite Did Not Save"
        )
    }

    @Test
    fun `Delete All Success`() = runTest {
        dbHelper.insertBreed("Poodle")
        dbHelper.insertBreed("Schnauzer")
        assertTrue(dbHelper.selectAllItems().first().isNotEmpty())
        dbHelper.deleteAll()

        assertTrue(
            dbHelper.selectAllItems().first().isEmpty(),
            "Delete All did not work"
        )
    }

    @Test
    fun `Delete Breed Success`() = runTest {
        // GIVEN

        val breeds = dbHelper.selectAllItems().first()
        // Could instead just check if the list was not empty and after deleting check
        // if the list is empty but I prefer to be more descriptive and dumb in UTs. That assumption
        // could be bad if someone messes with the number of items globally defined for tests,
        // currently we are just inserting the beagle breed into the db for testing but in the
        // future someone might change that for some reason and that can have a bad side effect
        // in this test.
        val beagleBreed = breeds.first { it.name == "Beagle" }
        assertTrue(dbHelper.selectAllItems().first().first().name == "Beagle")

        // WHEN

        dbHelper.deleteBreed(beagleBreed.id)
        val newBreeds = dbHelper.selectAllItems().first()

        // THEN

        assertTrue(
            newBreeds.none { it.name == "Beagle" },
            ""
        )
    }
}
