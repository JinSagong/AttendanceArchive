package com.jin.attendance_archive.model.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.jin.attendance_archive.model.data.DataOrganization

object OrganizationUtil {
    var mapOrganization = mutableStateMapOf<String, DataOrganization>()
        private set
    var mapOrganizationByOrgId = mutableStateMapOf<String, List<DataOrganization>>()
        private set

    @Composable
    fun remember() {
        mapOrganization = remember { mapOrganization }
        mapOrganizationByOrgId = remember { mapOrganizationByOrgId }
    }

    fun setOrganizationList(data: List<DataOrganization>) {
        mapOrganization.clear()
        mapOrganization.putAll(data.associateBy { it.id })
        mapOrganizationByOrgId.clear()
        mapOrganizationByOrgId.putAll(data.groupBy { it.orgId })
    }

    fun getOrganizationCount(hasFruit: Boolean) = mapOrganizationByOrgId.values.count {
        it.firstOrNull()?.region == if (!hasFruit) {
            if (UserUtil.isGumi()) 1 else 2
        } else {
            if (UserUtil.isGumi()) 3 else 4
        }
    }

    fun myOrganization(hasFruit: Boolean) = UserUtil.dataUser.value?.org?.sorted()
        ?.mapNotNull { orgId ->
            mapOrganizationByOrgId[orgId]?.mapNotNull {
                if (!hasFruit && it.region <= 2) it else if (hasFruit && it.region > 2) it else null
            }?.ifEmpty { null }?.let { orgId to it }
        }?.toMap().orEmpty()

    fun myCategory(hasFruit: Boolean) = UserUtil.dataUser.value?.org
        ?.sorted()
        ?.mapNotNull { orgId ->
            mapOrganizationByOrgId[orgId]?.firstOrNull {
                (!hasFruit && it.region <= 2) || (hasFruit && it.region > 2)
            }?.category1
        }
        ?.distinct()
        .orEmpty()

    fun getRegion(list: List<DataOrganization>) = 2 - (list.firstOrNull()?.region ?: 1) % 2

    fun update(data: DataOrganization) {
        mapOrganization[data.id] = data
        setOrganizationList(mapOrganization.values.toList())
    }
}