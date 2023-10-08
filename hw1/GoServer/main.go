package main

import (
	"fmt"
	"github.com/google/uuid"
    "github.com/gin-gonic/gin"
	"path/filepath"
	"net/http"
	"GoServer/models"
)

func main(){

   // Assign the DoGet function to a specific route
	router := gin.Default()
	//router.SetTrustedProxies([]string{"10.0.0.137"})
    router.GET("/albums/:id", func(c *gin.Context) {
		url := c.Request.URL.Path
		if url == "" || !isGetRequestValid(url) {
			c.JSON(http.StatusNotFound, gin.H{
				"message": "Not Found",
			})
			return
		}
	
		album := models.Album{
			Artist: "Jack",
			Title:  "singer",
			Year:   "2000",
		}
	
		c.JSON(http.StatusOK, album)
	})

	router.POST("/albums", func(c *gin.Context) {
		// Get uploaded image
		image, imageHeader, err := c.Request.FormFile("image")
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Image upload failed"})
			return
		}
		defer image.Close()

		imageSize := imageHeader.Size

		// Generate a random UUID for albumId
		randomUUID, err := uuid.NewRandom()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Internal Server Error"})
			return
		}
		randomId := randomUUID.String()

		// Construct the imageMetaData
		imageMetaData := models.ImageData{
			AlbumId:   randomId,
			ImageSize: fmt.Sprintf("%d", imageSize),
		}

		// Respond with JSON
		c.JSON(http.StatusOK, imageMetaData)
	})
    router.Run(":8080")
}

func isGetRequestValid(url string) bool {
	urlContents := len(splitURL(url))
	return urlContents != 3
}

func isPostRequestValid(url string) bool {
	urlContents := len(splitURL(url))
	return urlContents != 2
}

func splitURL(url string) []string {
	return filepath.SplitList(url)
}