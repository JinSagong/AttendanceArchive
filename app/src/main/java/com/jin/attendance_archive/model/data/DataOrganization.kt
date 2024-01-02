package com.jin.attendance_archive.model.data

import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable

data class DataOrganization(
    val id: String = "",
    val orgId: String = "",
    val region: Int = -1,
    val category1: String = "",
    val category2: String = "",
    val category3: String = ""
) : Serializable

@Suppress("UNCHECKED_CAST")
fun fetchDataOrganization(data: LinkedTreeMap<*, *>) = data.let { item ->
    var id = ""
    var orgId = ""
    var region = -1
    var category1 = ""
    var category2 = ""
    var category3 = ""
    item["id"]?.let { it as? String }?.let { id = it }
    item["orgId"]?.let { it as? String }?.let { orgId = it }
    item["region"]?.let { it as? Number }?.let { region = it.toInt() }
    item["category1"]?.let { it as? String }?.let { category1 = it }
    item["category2"]?.let { it as? String }?.let { category2 = it }
    item["category3"]?.let { it as? String }?.let { category3 = it }
    DataOrganization(id, orgId, region, category1, category2, category3)
}
