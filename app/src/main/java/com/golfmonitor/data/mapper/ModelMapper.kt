package com.golfmonitor.data.mapper

import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.golfmonitor.model.Course
import com.golfmonitor.model.TeeTimeDeal

fun Course.toEntity() = CourseEntity(
    id = id,
    name = name,
    county = county,
    latitude = latitude,
    longitude = longitude,
    postcode = postcode,
    greenFeeBaseline = greenFeeBaseline
)

fun CourseEntity.toModel() = Course(
    id = id,
    name = name,
    county = county,
    latitude = latitude,
    longitude = longitude,
    postcode = postcode,
    greenFeeBaseline = greenFeeBaseline
)

fun TeeTimeDeal.toEntity() = DealEntity(
    id = "$courseId-$date-$time",
    courseId = courseId,
    courseName = courseName,
    date = date,
    time = time,
    priceGbp = priceGbp,
    baselinePriceGbp = baselinePriceGbp,
    players = players,
    source = source,
    bookingUrl = bookingUrl
)

fun DealEntity.toModel() = TeeTimeDeal(
    courseId = courseId,
    courseName = courseName,
    date = date,
    time = time,
    priceGbp = priceGbp,
    baselinePriceGbp = baselinePriceGbp,
    players = players,
    source = source,
    bookingUrl = bookingUrl
)
