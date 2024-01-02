package com.jin.attendance_archive.model.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.jin.attendance_archive.model.data.DataPeople

object PeopleUtil {
    var mapPeople = mutableStateMapOf<String, DataPeople>()
        private set
    var mapPeopleByOrg = mutableStateMapOf<String, List<DataPeople>>()
        private set
    var mapPeopleByFirstOrg = mutableStateMapOf<String, List<DataPeople>>()
        private set

    @Composable
    fun remember() {
        mapPeople = remember { mapPeople }
        mapPeopleByOrg = remember { mapPeopleByOrg }
        mapPeopleByFirstOrg = remember { mapPeopleByFirstOrg }
    }

    fun setPeopleList(data: List<DataPeople>) {
        mapPeople.clear()
        mapPeople.putAll(data.associateBy { it.id })
        val tempMapByOrg = hashMapOf<String, ArrayList<DataPeople>>()
        val tempMapByFirstOrg = hashMapOf<String, ArrayList<DataPeople>>()
        data.filter { item -> item.activated }.forEach { people ->
            people.org.forEachIndexed { idx, org ->
                OrganizationUtil.mapOrganization[org]?.region?.let { region ->
                    if ((UserUtil.isGumi() && (region == 1 || region == 3)) || (!UserUtil.isGumi() && (region == 2 || region == 4))) {
                        if (idx == 0) tempMapByFirstOrg.getOrPut(org) { arrayListOf() }.add(people)
                        tempMapByOrg.getOrPut(org) { arrayListOf() }.add(people)
                    }
                }
            }
            if (people.org.isEmpty()) {
                tempMapByFirstOrg.getOrPut("") { arrayListOf() }.add(people)
                tempMapByOrg.getOrPut("") { arrayListOf() }.add(people)
            }
        }
        mapPeopleByOrg.clear()
        mapPeopleByOrg.putAll(tempMapByOrg)
        mapPeopleByFirstOrg.clear()
        mapPeopleByFirstOrg.putAll(tempMapByFirstOrg)
    }

    fun update(data: DataPeople) {
        mapPeople[data.id] = data
        setPeopleList(mapPeople.values.toList())
    }

    fun getRegion(data: DataPeople) =
        data.org.firstOrNull()?.let { org -> OrganizationUtil.mapOrganization[org]?.region }
}