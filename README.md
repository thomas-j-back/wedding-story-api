## API for Wedding Story Front End
Maven project
Using Spring to handle incoming api request for image generation, s3 management.

Requires cloudflare R2 bucket, openai subscription (please see example.env for required env vars)

Creates Image Generation jobs based on user given images. Meant to facilitate styling photos and creating story images
that ultimately will be used to generate a static site displaying a couples wedding story to be sent with wedding evites.

To run get all Maven dependencies
` mvn spring-boot:run  `
