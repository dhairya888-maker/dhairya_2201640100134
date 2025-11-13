const imageElement = document.getElementById("frame") as HTMLImageElement;
const statsElement = document.getElementById("stats") as HTMLDivElement;

const SAMPLE_BASE64 =
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg==";

const FPS_VALUE = 12.3;
const RES_WIDTH = 1280;
const RES_HEIGHT = 720;

function updateViewer(): void {
  imageElement.src = SAMPLE_BASE64;
  statsElement.innerText = `FPS: ${FPS_VALUE.toFixed(1)} | Res: ${RES_WIDTH}x${RES_HEIGHT}`;
}

document.addEventListener("DOMContentLoaded", updateViewer);

