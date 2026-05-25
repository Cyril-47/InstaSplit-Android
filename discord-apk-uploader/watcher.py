import os
import time
import json
import urllib.request
import urllib.error

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config.json")

def load_config():
    if not os.path.exists(CONFIG_PATH):
        print(f"Config file not found at {CONFIG_PATH}. Creating template...")
        default_config = {
            "webhook_url": "YOUR_DISCORD_WEBHOOK_URL_HERE",
            "apk_path": "K:\\InstaSplit Android ver\\app\\build\\outputs\\apk\\release\\app-release.apk",
            "build_number": 1
        }
        with open(CONFIG_PATH, "w") as f:
            json.dump(default_config, f, indent=2)
        return default_config
        
    with open(CONFIG_PATH, "r") as f:
        config = json.load(f)
        # Ensure build_number is set
        if "build_number" not in config:
            config["build_number"] = 1
        return config

def save_config(config):
    with open(CONFIG_PATH, "w") as f:
        json.dump(config, f, indent=2)

def wait_for_file_stability(file_path):
    """Wait until the file size is stable (not changing) and can be opened for reading."""
    last_size = -1
    stable_count = 0
    # Wait for up to 30 seconds
    for _ in range(60):
        try:
            if not os.path.exists(file_path):
                return False
            current_size = os.path.getsize(file_path)
            # Try opening the file to check if it's locked
            with open(file_path, 'rb') as f:
                pass
            if current_size == last_size and current_size > 0:
                stable_count += 1
            else:
                last_size = current_size
                stable_count = 0
            
            if stable_count >= 3:
                return True
        except IOError:
            # File is locked or still being written
            stable_count = 0
        time.sleep(0.5)
    return False

def send_file_to_webhook(webhook_url, file_path, build_number):
    if not os.path.exists(file_path):
        print(f"Error: File not found at {file_path}")
        return False
        
    if not wait_for_file_stability(file_path):
        print("Error: File was not stable or was locked for too long.")
        return False

    filename = os.path.basename(file_path)
    file_size_mb = os.path.getsize(file_path) / (1024 * 1024)
    print(f"Preparing to upload {filename} ({file_size_mb:.2f} MB) as Build #{build_number}...")
    
    boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
    
    # Read the file content
    with open(file_path, "rb") as f:
        file_content = f.read()
        
    # Construct multipart request body
    # Discord supports 'content' for text and 'file' (or 'files[0]') for attachments
    body_parts = []
    
    # Add a message content part
    message = f"🚀 **New APK Build Uploaded! (Build #{build_number})**\n📄 **Filename:** `{filename}`\n📦 **Size:** `{file_size_mb:.2f} MB`\n🕒 **Timestamp:** `{time.strftime('%Y-%m-%d %H:%M:%S')}`"
    body_parts.append(
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="content"\r\n\r\n'
        f'{message}\r\n'
    )
    
    # Add file part
    file_header = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
        "Content-Type: application/vnd.android.package-archive\r\n\r\n"
    )
    
    body = b""
    for part in body_parts:
        body += part.encode('utf-8')
        
    body += file_header.encode('utf-8') + file_content + f"\r\n--{boundary}--\r\n".encode('utf-8')
    
    req = urllib.request.Request(
        webhook_url,
        data=body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
        },
        method="POST"
    )
    
    try:
        with urllib.request.urlopen(req) as response:
            print("[SUCCESS] APK uploaded to Discord.")
            return True
    except urllib.error.HTTPError as e:
        print(f"[ERROR] HTTP Error: {e.code} - {e.reason}")
        try:
            print(e.read().decode('utf-8', errors='ignore'))
        except:
            pass
        return False
    except Exception as e:
        print(f"[ERROR] Error during upload: {e}")
        return False

def main():
    config = load_config()
    webhook_url = config.get("webhook_url")
    apk_path = config.get("apk_path")
    
    if not webhook_url or "YOUR_DISCORD_WEBHOOK" in webhook_url:
        print("[!] Please configure your Discord Webhook URL in config.json before running.")
        return
        
    print("==================================================")
    print("      Discord APK Uploader Bot (Active)           ")
    print("==================================================")
    print(f"Target APK: {apk_path}")
    print(f"Watching for changes. Press Ctrl+C to stop.")
    print("==================================================")
    
    last_mtime = None
    if os.path.exists(apk_path):
        last_mtime = os.path.getmtime(apk_path)
        print("Current APK detected. Checking for future updates...")
    else:
        print("APK not found yet. Monitoring path for file creation...")
        
    while True:
        try:
            if os.path.exists(apk_path):
                current_mtime = os.path.getmtime(apk_path)
                if last_mtime is None:
                    print("\n[+] New APK file created!")
                    last_mtime = current_mtime
                    # Reload config to get the latest build number
                    config = load_config()
                    build_num = config.get("build_number", 1)
                    if send_file_to_webhook(webhook_url, apk_path, build_num):
                        config["build_number"] = build_num + 1
                        save_config(config)
                elif current_mtime > last_mtime:
                    print("\n[+] Update detected on APK file!")
                    # Store mtime immediately to avoid re-triggering while uploading
                    last_mtime = current_mtime
                    # Reload config to get the latest build number
                    config = load_config()
                    build_num = config.get("build_number", 1)
                    if send_file_to_webhook(webhook_url, apk_path, build_num):
                        config["build_number"] = build_num + 1
                        save_config(config)
            else:
                if last_mtime is not None:
                    print("\n[-] APK file removed.")
                    last_mtime = None
        except KeyboardInterrupt:
            print("\nStopping watcher. Goodbye!")
            break
        except Exception as e:
            print(f"\n[!] Error checking file: {e}")
            
        time.sleep(2)

if __name__ == "__main__":
    main()
