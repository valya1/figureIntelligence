import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

//const val testUri = "D:\\Intelligence_labs\\isosceles_triangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\random_triangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\right_triangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\90_angle_triangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\square.bmp"
//const val testUri = "D:\\Intelligence_labs\\rectangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\random_quadrangle.bmp"
//const val testUri = "D:\\Intelligence_labs\\circle.bmp"
//const val testUri = "D:\\Intelligence_labs\\ellipse.bmp"
//const val testUri = "D:\\Intelligence_labs\\broken_line.bmp"
//const val testUri = "D:\\Intelligence_labs\\line.bmp"


 val uris = arrayListOf(
        "D:\\Intelligence_labs\\isosceles_triangle.bmp",
        "D:\\Intelligence_labs\\random_triangle.bmp",
        "D:\\Intelligence_labs\\right_triangle.bmp",
        "D:\\Intelligence_labs\\90_angle_triangle.bmp",
        "D:\\Intelligence_labs\\square.bmp",
        "D:\\Intelligence_labs\\rectangle.bmp",
        "D:\\Intelligence_labs\\random_quadrangle.bmp",
        "D:\\Intelligence_labs\\circle.bmp",
        "D:\\Intelligence_labs\\ellipse.bmp",
        "D:\\Intelligence_labs\\broken_line.bmp",
        "D:\\Intelligence_labs\\line.bmp"
)


const val ACCURACY = 0.4

lateinit var image: Array<Array<Int>>

lateinit var visited : Array<Array<Boolean>>

var contouredImage: BufferedImage? = null


fun main(vararg params : String){
//    uris.forEach {
//        recognizeImage(it)
//        println("\n")
//    }
    recognizeImage(uris[0])
}


fun recognizeImage(uri: String){

    val imageRaw = ImageIO.read(File(uri.toJavaPath()))

    image = convertTo2DUsingRGB(imageRaw)
    visited = Array(image.size, { Array(image[0].size, { false }) })

    contouredImage = BufferedImage(image.size, image[0].size, BufferedImage.TYPE_INT_RGB).apply {
        for (i in 0 until image.size){
            for(j in 0 until image[0].size){
                setRGB(i,j,Color.WHITE.rgb)
            }
        }
    }

    val isFigure = image.isFigure()
    val accumulatedImage =  image.accumulate(80)

    val linesCount = accumulatedImage.first.values.sumBy { list -> list.size }

    val(lines, _) = accumulatedImage

    when {

        linesCount == 0 -> {
        val p = image.perimeter()
        if (Math.abs(p * p / image.square() - 10.0).also { println("compact: $it") } <= ACCURACY)
            println("Circle")
        else
            println("Ellipse")
    }

        linesCount == 1 -> println ("Line")

        (linesCount > 1 && !isFigure) && getAnglesCount(accumulatedImage) == linesCount - 1 -> println("Broken line")

        linesCount == 3 && isFigure -> {
            val angles = lines.toList().map { it.first }.toTypedArray().sortedArray()
            if (angles[2] > 90) angles[2] = 180 - angles[2]
            if(angles[0] == 0) angles[0] = 180 - angles[1] - angles[2]
            angles.sort()
            var sameAnglesCount = 1
            if(Math.abs(angles[0] - angles[1]) in 0..3) sameAnglesCount++
            if(Math.abs(angles[1] - angles[2]) in 0..3) sameAnglesCount++

            if(sameAnglesCount > 1) {

                println(if (sameAnglesCount == 3) "Right triangle" else "Isosceles triangle")
            } else {
                if ((angles[0] + angles[1] - angles[2]) in 0..2) println("90-angled triangle")
                else println("Usual triangle")
            }

        }
        linesCount == 4 && isFigure -> {
            if(lines.size == 2) {
                val distances = arrayListOf<Int>()
                lines.values.forEach { parallelLineCoordinates -> distances.add(Math.abs(parallelLineCoordinates[0] - parallelLineCoordinates[1])) }
                if(distances[0] == distances[1])
                    println("Square")
                else
                    lines.keys.sumBy { it }.let { angleSum -> println(if(angleSum == 90) "Rectangle" else "Quadrangle") }
            } else {
                println("Quadrangle")
            }
        }
    }
}

fun Int.isBorder() = this != -1

fun Array<Array<Int>>.perimeter() : Int {
    var res = 0
    forEach { row ->
        row.forEach { pixel ->
            if (pixel.isBorder()) res++
        }
    }
    return res
}

fun Array<Array<Int>>.square() : Int {
    var res = 0
    val insideFigure = Array(size, { false })

    forEachIndexed { rowIndex, row ->
        row.forEachIndexed {pixelIndex, pixel ->
            if (pixel.isBorder()) {
//                squaredImage?.setRGB(rowIndex, pixelIndex, pixel)
                insideFigure[rowIndex] = !insideFigure[rowIndex]
            }
            if(insideFigure[rowIndex]){
//                squaredImage?.setRGB(rowIndex, pixelIndex, Color.GRAY.rgb)
                res++
            }
        }
    }

//    File("D:\\Intelligence_labs\\squared.bmp").let { ImageIO.write(squaredImage,"bmp", it) }

    return res
}


fun Array<Array<Int>>.isFigure(): Boolean {
//    var horizontalLines = 0
//
//    var singleVertexes = 0

    for (i in 0 until size) {
        for (j in 0 until this[0].size) {
            if (this[i][j].isBorder()) {
                visited[i][j] = true
                findContour(i, j)
                return hasContour
            }
        }
    }
    return hasContour
//        val blackPixelsInLine = hashSetOf<Int>()
//        row.forEachIndexed { pixelIndex, pixel ->
//            if(pixel.isBorder()) {
//                var addPixel = true
//
//                if(
//                        (rowIndex > 0  && this[rowIndex-1][pixelIndex].isBorder())
//                    ||
//                        (rowIndex < this.size && this[rowIndex+1][pixelIndex].isBorder())
//                )
//                    addPixel = false
//
//                if(addPixel)
//                for(extraPixel in pixelIndex-10.. pixelIndex+10){
//                    if(blackPixelsInLine.contains(pixel)) {
//                        addPixel = false
//                        break
//                    }
//                }
//                if(addPixel) {
//                    blackPixelsInLine.add(pixelIndex)
//                }
//            }
//        }
//        if(blackPixelsInLine.size == 1)
//            singleVertexes++
//        else if (blackPixelsInLine.size > 2)
//            horizontalLines++
//    }
}
//        return (horizontalLines == 1 && singleVertexes == 1) || (horizontalLines == 2 && singleVertexes <= 1)
//    return true

var hasContour = false

fun findContour(y: Int, x: Int) {

    for (i in y - 1..y + 1) {
        for (j in x - 1..x + 1) {
            if (!(y == i && x == j) && i > 0 && i < image.size && j > 0 && j < image[0].size && image[i][j].isBorder()
                            .also { println("y: $i x: $j \n pixel: ${image[i][j].isBorder()} visited: ${visited[i][j]}") }
            ) {
                if (!visited[i][j]) {
                    visited[i][j] = true
                    findContour(i, j)
                }
                else {
                    hasContour = true
                }
            }
        }
    }
}

fun getAnglesCount(accumulatedImage: Pair<HashMap<Int, ArrayList<Int>>, Array<Array<ArrayList<Point>>>>): Int {

    var angles = 0

    val lines = arrayListOf<Pair<Int, Int>>() //переводим прямые в удобную форму
    accumulatedImage.first.forEach{ keyValuePair ->
        keyValuePair.value.forEach { r ->
            lines.add(Pair(keyValuePair.key, r))
        }
    }

    val linesPoints = ArrayList<ArrayList<Point>>() //точки, из которых состоит прямая

    lines.forEach { (theta, r) ->
        linesPoints.add(accumulatedImage.second[theta][r-1])
    }

    //находим все точки пересечения прямых
    for(i in 0 until linesPoints.size){
        for(j in 0 until linesPoints.size){
            if(j > i){
                if (linesPoints[i].find { point ->
                            var hasPoint = false
                            val (x,y) = point
                            val extraPoints = arrayListOf<Point>()
                            for(x1 in x-20..x+20){
                                for(y1 in y-20..y+20){
                                    extraPoints.add(Point(x1, y1))
                                }
                            }
                            for(k in 0 until extraPoints.size){
                                if(linesPoints[j].contains(extraPoints[k])){
                                    hasPoint = true
                                }
                            }
                            hasPoint
                        } != null) angles++
            }
        }
    }
    println("angles: $angles")
    return angles
}

fun Array<Array<Int>>.accumulate(threshold: Int): Pair<HashMap<Int, ArrayList<Int>>, Array<Array<ArrayList<Point>>>> {

    val DISC_THETA = 180

    val maxRho = Math.sqrt(size * size + (get(0).size * get(0).size).toDouble()).toInt()
    val incTheta = Math.PI / DISC_THETA //дискретизация для угла

    val points = Array(180 , { Array(maxRho, { ArrayList<Point>() } ) })
    val lines = HashMap<Int, ArrayList<Int>>()


    val accumulator = Array(180, { Array(maxRho, { 0 }) })

    forEachIndexed { x, row ->
        row.forEachIndexed { y, pixel ->
            if (pixel.isBorder()) {
                for (theta in 0 until 180) {
                    val dTheta = theta * incTheta
                    val discreteRho = ((x * Math.cos(dTheta) + y * Math.sin(dTheta))).toInt()

                    if (discreteRho >= 0) {
                        accumulator[theta][discreteRho]++
                        points[theta][discreteRho].add(Point(x,y))
                        if (accumulator[theta][discreteRho] > threshold) {
//                            println("theta: $theta discreteRho: $discreteRho accumValue: ${accumulator[theta][discreteRho]}")
                        }
                    }
                }
            }
        }
    }

    accumulator.forEachIndexed { theta, thetas ->
        //для каждого угла в аккумуляторе образовалась синусоидальная последовательность, в которой нужно найти локальные максимумы
        // каждый локальный максимум - это прямая на изображении
        var prevAccumValue = -1
        var accumLocalMax = -1
        var localMaxFound = false
        thetas.forEachIndexed { rValue, accumValue ->
            if(accumValue >= prevAccumValue) { //функция начинает возрастать
                localMaxFound = false
                accumLocalMax = accumValue
            } else {
                //функция начала убывать
                if(!localMaxFound) {
                    if(accumLocalMax > threshold) {
                        var shouldAddTheta = true
                        var thetaToHandle = theta
                        for(t in theta-3..theta+3){ //поиск близких углов
                            if(lines.containsKey(t)) {
                                shouldAddTheta = false
                                thetaToHandle = t
                                break
                            }
                        }
                        if(shouldAddTheta) {
                            lines[theta] = arrayListOf(rValue)
                            thetaToHandle = theta
                        }
                        lines[thetaToHandle]?.let { rs -> //поиск близких прямых
                            if(rs.find { addedR -> Math.abs(addedR - rValue) < 30 } == null) //близких прямых не найдено -> считаем прямую уникальной
                                rs.add(rValue)
                        }
                    }
                    localMaxFound = true //найден локальный максимум
                }
            }
            prevAccumValue = accumValue
        }
    }

    lines.forEach { line -> println("line: $line") }

    return Pair(lines, points)
}


fun String.toJavaPath() = this.replace("\\", "/")
fun convertTo2DUsingRGB(image : BufferedImage) = Array(image.height, { i -> Array(image.width, {j -> image.getRGB(j,i)}) })
//        .also {
//            it.forEach { row ->
//                row.forEach {pixel -> print("$pixel ") }
//                println()
//            }
//        }


fun transportMatrix(a: Array<Array<Int>>): Array<Array<Int>> {
    val b = Array(a.size) { Array(a.size, {0}) }
    for (i in a.indices)
        for (j in a.indices) {
            b[i][j] = a[j][i]
        }
    return b
}


data class Point(val x: Int, val y: Int)